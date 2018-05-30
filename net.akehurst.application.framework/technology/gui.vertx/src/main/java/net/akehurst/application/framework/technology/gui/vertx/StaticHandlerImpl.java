/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.akehurst.application.framework.technology.gui.vertx;

import java.io.File;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Http2PushMapping;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.impl.LRUCache;
import io.vertx.ext.web.impl.Utils;

public class StaticHandlerImpl implements StaticHandler {

    private static final Logger log = LoggerFactory.getLogger(StaticHandlerImpl.class);

    private final DateFormat dateTimeFormatter = Utils.createRFC1123DateTimeFormatter();
    private Map<String, CacheEntry> propsCache;
    private String webRoot = StaticHandler.DEFAULT_WEB_ROOT;
    private long maxAgeSeconds = StaticHandler.DEFAULT_MAX_AGE_SECONDS; // One day
    private boolean directoryListing = StaticHandler.DEFAULT_DIRECTORY_LISTING;
    private String directoryTemplateResource = StaticHandler.DEFAULT_DIRECTORY_TEMPLATE;
    private String directoryTemplate;
    private boolean includeHidden = StaticHandler.DEFAULT_INCLUDE_HIDDEN;
    private boolean filesReadOnly = StaticHandler.DEFAULT_FILES_READ_ONLY;
    private boolean cachingEnabled = StaticHandler.DEFAULT_CACHING_ENABLED;
    private long cacheEntryTimeout = StaticHandler.DEFAULT_CACHE_ENTRY_TIMEOUT;
    private String indexPage = StaticHandler.DEFAULT_INDEX_PAGE;
    private List<Http2PushMapping> http2PushMappings;
    private int maxCacheSize = StaticHandler.DEFAULT_MAX_CACHE_SIZE;
    private boolean rangeSupport = StaticHandler.DEFAULT_RANGE_SUPPORT;
    private boolean allowRootFileSystemAccess = StaticHandler.DEFAULT_ROOT_FILESYSTEM_ACCESS;
    private boolean sendVaryHeader = StaticHandler.DEFAULT_SEND_VARY_HEADER;
    private String defaultContentEncoding = Charset.defaultCharset().name();

    // These members are all related to auto tuning of synchronous vs asynchronous file system access
    private static int NUM_SERVES_TUNING_FS_ACCESS = 1000;
    private boolean alwaysAsyncFS = StaticHandler.DEFAULT_ALWAYS_ASYNC_FS;
    private long maxAvgServeTimeNanoSeconds = StaticHandler.DEFAULT_MAX_AVG_SERVE_TIME_NS;
    private boolean tuning = StaticHandler.DEFAULT_ENABLE_FS_TUNING;
    private long totalTime;
    private long numServesBlocking;
    private boolean useAsyncFS;
    private long nextAvgCheck = StaticHandlerImpl.NUM_SERVES_TUNING_FS_ACCESS;

    private final ClassLoader classLoader;

    public StaticHandlerImpl(final String root, final ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.setRoot(root);
    }

    public StaticHandlerImpl() {
        this.classLoader = null;
    }

    private String directoryTemplate(final Vertx vertx) {
        if (this.directoryTemplate == null) {
            this.directoryTemplate = Utils.readFileToString(vertx, this.directoryTemplateResource);
        }
        return this.directoryTemplate;
    }

    /**
     * Create all required header so content can be cache by Caching servers or Browsers
     *
     * @param request
     *            base HttpServerRequest
     * @param props
     *            file properties
     */
    private void writeCacheHeaders(final HttpServerRequest request, final FileProps props) {

        final MultiMap headers = request.response().headers();

        if (this.cachingEnabled) {
            // We use cache-control and last-modified
            // We *do not use* etags and expires (since they do the same thing - redundant)
            headers.set("cache-control", "public, max-age=" + this.maxAgeSeconds);
            headers.set("last-modified", this.dateTimeFormatter.format(props.lastModifiedTime()));
            // We send the vary header (for intermediate caches)
            // (assumes that most will turn on compression when using static handler)
            if (this.sendVaryHeader && request.headers().contains("accept-encoding")) {
                headers.set("vary", "accept-encoding");
            }
        }

        // date header is mandatory
        headers.set("date", this.dateTimeFormatter.format(new Date()));
    }

    @Override
    public void handle(final RoutingContext context) {
        final HttpServerRequest request = context.request();
        if (request.method() != HttpMethod.GET && request.method() != HttpMethod.HEAD) {
            if (StaticHandlerImpl.log.isTraceEnabled()) {
                StaticHandlerImpl.log.trace("Not GET or HEAD so ignoring request");
            }
            context.next();
        } else {
            String path = Utils.removeDots(Utils.urlDecode(context.normalisedPath(), false));
            // if the normalized path is null it cannot be resolved
            if (path == null) {
                StaticHandlerImpl.log.warn("Invalid path: " + context.request().path());
                context.next();
                return;
            }

            // only root is known for sure to be a directory. all other directories must be identified as such.
            if (!this.directoryListing && "/".equals(path)) {
                path = this.indexPage;
            }

            // can be called recursive for index pages
            this.sendStatic(context, path);

        }
    }

    private void sendStatic(final RoutingContext context, final String path) {

        String file = null;

        if (!this.includeHidden) {
            file = this.getFile(path, context);
            final int idx = file.lastIndexOf('/');
            final String name = file.substring(idx + 1);
            if (name.length() > 0 && name.charAt(0) == '.') {
                // skip
                context.next();
                return;
            }
        }

        // Look in cache
        CacheEntry entry;
        if (this.cachingEnabled) {
            entry = this.propsCache().get(path);
            if (entry != null) {
                final HttpServerRequest request = context.request();
                if ((this.filesReadOnly || !entry.isOutOfDate()) && entry.shouldUseCached(request)) {
                    context.response().setStatusCode(HttpResponseStatus.NOT_MODIFIED.code()).end();
                    return;
                }
            }
        }

        if (file == null) {
            file = this.getFile(path, context);
        }

        final String sfile = file;

        // verify if the file exists
        this.isFileExisting(context, sfile, exists -> {
            if (exists.failed()) {
                context.fail(exists.cause());
                return;
            }

            // file does not exist, continue...
            if (!exists.result()) {
                context.next();
                return;
            }

            // Need to read the props from the filesystem
            this.getFileProps(context, sfile, res -> {
                if (res.succeeded()) {
                    final FileProps fprops = res.result();
                    if (fprops == null) {
                        // File does not exist
                        context.next();
                    } else if (fprops.isDirectory()) {
                        this.sendDirectory(context, path, sfile);
                    } else {
                        this.propsCache().put(path, new CacheEntry(fprops, System.currentTimeMillis()));
                        this.sendFile(context, sfile, fprops);
                    }
                } else {
                    context.fail(res.cause());
                }
            });
        });
    }

    private void sendDirectory(final RoutingContext context, final String path, final String file) {
        if (this.directoryListing) {
            this.sendDirectoryListing(file, context);
        } else if (this.indexPage != null) {
            // send index page
            String indexPath;
            if (path.endsWith("/") && this.indexPage.startsWith("/")) {
                indexPath = path + this.indexPage.substring(1);
            } else if (!path.endsWith("/") && !this.indexPage.startsWith("/")) {
                indexPath = path + "/" + this.indexPage.substring(1);
            } else {
                indexPath = path + this.indexPage;
            }
            // recursive call
            this.sendStatic(context, indexPath);

        } else {
            // Directory listing denied
            context.fail(HttpResponseStatus.FORBIDDEN.code());
        }
    }

    private <T> T wrapInTCCLSwitch(final Callable<T> callable) {
        try {
            if (this.classLoader == null) {
                return callable.call();
            } else {
                final ClassLoader original = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(this.classLoader);
                    return callable.call();
                } finally {
                    Thread.currentThread().setContextClassLoader(original);
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void isFileExisting(final RoutingContext context, final String file, final Handler<AsyncResult<Boolean>> resultHandler) {
        final FileSystem fs = context.vertx().fileSystem();
        this.wrapInTCCLSwitch(() -> fs.exists(file, resultHandler));
    }

    private synchronized void getFileProps(final RoutingContext context, final String file, final Handler<AsyncResult<FileProps>> resultHandler) {
        final FileSystem fs = context.vertx().fileSystem();
        if (this.alwaysAsyncFS || this.useAsyncFS) {
            this.wrapInTCCLSwitch(() -> fs.props(file, resultHandler));
        } else {
            // Use synchronous access - it might well be faster!
            long start = 0;
            if (this.tuning) {
                start = System.nanoTime();
            }
            try {
                final FileProps props = this.wrapInTCCLSwitch(() -> fs.propsBlocking(file));

                if (this.tuning) {
                    final long end = System.nanoTime();
                    final long dur = end - start;
                    this.totalTime += dur;
                    this.numServesBlocking++;
                    if (this.numServesBlocking == Long.MAX_VALUE) {
                        // Unlikely.. but...
                        this.resetTuning();
                    } else if (this.numServesBlocking == this.nextAvgCheck) {
                        final double avg = (double) this.totalTime / this.numServesBlocking;
                        if (avg > this.maxAvgServeTimeNanoSeconds) {
                            this.useAsyncFS = true;
                            StaticHandlerImpl.log
                                    .info("Switching to async file system access in static file server as fs access is slow! (Average access time of " + avg
                                            + " ns)");
                            this.tuning = false;
                        }
                        this.nextAvgCheck += StaticHandlerImpl.NUM_SERVES_TUNING_FS_ACCESS;
                    }
                }
                resultHandler.handle(Future.succeededFuture(props));
            } catch (final RuntimeException e) {
                resultHandler.handle(Future.failedFuture(e.getCause()));
            }
        }
    }

    private void resetTuning() {
        // Reset
        this.nextAvgCheck = StaticHandlerImpl.NUM_SERVES_TUNING_FS_ACCESS;
        this.totalTime = 0;
        this.numServesBlocking = 0;
    }

    private static final Pattern RANGE = Pattern.compile("^bytes=(\\d+)-(\\d*)$");

    protected void sendFile(final RoutingContext context, final String file, final FileProps fileProps) {
        final HttpServerRequest request = context.request();

        Long offset = null;
        Long end = null;
        MultiMap headers = null;

        if (this.rangeSupport) {
            // check if the client is making a range request
            final String range = request.getHeader("Range");
            // end byte is length - 1
            end = fileProps.size() - 1;

            if (range != null) {
                final Matcher m = StaticHandlerImpl.RANGE.matcher(range);
                if (m.matches()) {
                    try {
                        String part = m.group(1);
                        // offset cannot be empty
                        offset = Long.parseLong(part);
                        // offset must fall inside the limits of the file
                        if (offset < 0 || offset >= fileProps.size()) {
                            throw new IndexOutOfBoundsException();
                        }
                        // length can be empty
                        part = m.group(2);
                        if (part != null && part.length() > 0) {
                            // ranges are inclusive
                            end = Math.min(end, Long.parseLong(part));
                            // end offset must not be smaller than start offset
                            if (end < offset) {
                                throw new IndexOutOfBoundsException();
                            }
                        }
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        context.response().putHeader("Content-Range", "bytes */" + fileProps.size());
                        context.fail(HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE.code());
                        return;
                    }
                }
            }

            // notify client we support range requests
            headers = request.response().headers();
            headers.set("Accept-Ranges", "bytes");
            // send the content length even for HEAD requests
            headers.set("Content-Length", Long.toString(end + 1 - (offset == null ? 0 : offset)));
        }

        this.writeCacheHeaders(request, fileProps);

        if (request.method() == HttpMethod.HEAD) {
            request.response().end();
        } else {
            if (this.rangeSupport && offset != null) {
                // must return content range
                headers.set("Content-Range", "bytes " + offset + "-" + end + "/" + fileProps.size());
                // return a partial response
                request.response().setStatusCode(HttpResponseStatus.PARTIAL_CONTENT.code());

                // Wrap the sendFile operation into a TCCL switch, so the file resolver would find the file from the set
                // classloader (if any).
                final Long finalOffset = offset;
                final Long finalEnd = end;
                this.wrapInTCCLSwitch(() -> {
                    // guess content type
                    final String contentType = MimeMapping.getMimeTypeForFilename(file);
                    if (contentType != null) {
                        if (contentType.startsWith("text")) {
                            request.response().putHeader("Content-Type", contentType + ";charset=" + this.defaultContentEncoding);
                        } else {
                            request.response().putHeader("Content-Type", contentType);
                        }
                    }

                    return request.response().sendFile(file, finalOffset, finalEnd + 1, res2 -> {
                        if (res2.failed()) {
                            context.fail(res2.cause());
                        }
                    });
                });
            } else {
                // Wrap the sendFile operation into a TCCL switch, so the file resolver would find the file from the set
                // classloader (if any).
                this.wrapInTCCLSwitch(() -> {
                    // guess content type
                    final String contentType = MimeMapping.getMimeTypeForFilename(file);
                    if (contentType != null) {
                        if (contentType.startsWith("text")) {
                            request.response().putHeader("Content-Type", contentType + ";charset=" + this.defaultContentEncoding);
                        } else {
                            request.response().putHeader("Content-Type", contentType);
                        }
                    }

                    // http2 pushing support
                    if (request.version() == HttpVersion.HTTP_2 && this.http2PushMappings != null) {
                        for (final Http2PushMapping dependency : this.http2PushMappings) {
                            if (!dependency.isNoPush()) {
                                final String dep = this.webRoot + "/" + dependency.getFilePath();
                                final HttpServerResponse response = request.response();

                                // get the file props
                                this.getFileProps(context, dep, filePropsAsyncResult -> {
                                    if (filePropsAsyncResult.succeeded()) {
                                        // push
                                        this.writeCacheHeaders(request, filePropsAsyncResult.result());
                                        response.push(HttpMethod.GET, "/" + dependency.getFilePath(), pushAsyncResult -> {
                                            if (pushAsyncResult.succeeded()) {
                                                final HttpServerResponse res = pushAsyncResult.result();
                                                final String depContentType = MimeMapping.getMimeTypeForExtension(file);
                                                if (depContentType != null) {
                                                    if (depContentType.startsWith("text")) {
                                                        res.putHeader("Content-Type", contentType + ";charset=" + this.defaultContentEncoding);
                                                    } else {
                                                        res.putHeader("Content-Type", contentType);
                                                    }
                                                }
                                                res.sendFile(this.webRoot + "/" + dependency.getFilePath());
                                            }
                                        });
                                    }
                                });
                            }
                        }

                    } else if (this.http2PushMappings != null) {
                        // Link preload when file push is not supported
                        final HttpServerResponse response = request.response();
                        final List<String> links = new ArrayList<>();
                        for (final Http2PushMapping dependency : this.http2PushMappings) {
                            final String dep = this.webRoot + "/" + dependency.getFilePath();
                            // get the file props
                            this.getFileProps(context, dep, filePropsAsyncResult -> {
                                if (filePropsAsyncResult.succeeded()) {
                                    // push
                                    this.writeCacheHeaders(request, filePropsAsyncResult.result());
                                    links.add("<" + dependency.getFilePath() + ">; rel=preload; as=" + dependency.getExtensionTarget()
                                            + (dependency.isNoPush() ? "; nopush" : ""));
                                }
                            });
                        }
                        response.putHeader("Link", links);
                    }

                    return request.response().sendFile(file, res2 -> {
                        if (res2.failed()) {
                            context.fail(res2.cause());
                        }
                    });
                });
            }
        }
    }

    @Override
    public StaticHandler setAllowRootFileSystemAccess(final boolean allowRootFileSystemAccess) {
        this.allowRootFileSystemAccess = allowRootFileSystemAccess;
        return this;
    }

    @Override
    public StaticHandler setWebRoot(final String webRoot) {
        this.setRoot(webRoot);
        return this;
    }

    @Override
    public StaticHandler setFilesReadOnly(final boolean readOnly) {
        this.filesReadOnly = readOnly;
        return this;
    }

    @Override
    public StaticHandler setMaxAgeSeconds(final long maxAgeSeconds) {
        if (maxAgeSeconds < 0) {
            throw new IllegalArgumentException("timeout must be >= 0");
        }
        this.maxAgeSeconds = maxAgeSeconds;
        return this;
    }

    @Override
    public StaticHandler setMaxCacheSize(final int maxCacheSize) {
        if (maxCacheSize < 1) {
            throw new IllegalArgumentException("maxCacheSize must be >= 1");
        }
        this.maxCacheSize = maxCacheSize;
        return this;
    }

    @Override
    public StaticHandler setCachingEnabled(final boolean enabled) {
        this.cachingEnabled = enabled;
        return this;
    }

    @Override
    public StaticHandler setDirectoryListing(final boolean directoryListing) {
        this.directoryListing = directoryListing;
        return this;
    }

    @Override
    public StaticHandler setDirectoryTemplate(final String directoryTemplate) {
        this.directoryTemplateResource = directoryTemplate;
        this.directoryTemplate = null;
        return this;
    }

    @Override
    public StaticHandler setEnableRangeSupport(final boolean enableRangeSupport) {
        this.rangeSupport = enableRangeSupport;
        return this;
    }

    @Override
    public StaticHandler setIncludeHidden(final boolean includeHidden) {
        this.includeHidden = includeHidden;
        return this;
    }

    @Override
    public StaticHandler setCacheEntryTimeout(final long timeout) {
        if (timeout < 1) {
            throw new IllegalArgumentException("timeout must be >= 1");
        }
        this.cacheEntryTimeout = timeout;
        return this;
    }

    @Override
    public StaticHandler setIndexPage(String indexPage) {
        Objects.requireNonNull(indexPage);
        if (!indexPage.startsWith("/")) {
            indexPage = "/" + indexPage;
        }
        this.indexPage = indexPage;
        return this;
    }

    @Override
    public StaticHandler setAlwaysAsyncFS(final boolean alwaysAsyncFS) {
        this.alwaysAsyncFS = alwaysAsyncFS;
        return this;
    }

    @Override
    public StaticHandler setHttp2PushMapping(final List<Http2PushMapping> http2PushMap) {
        if (http2PushMap != null) {
            this.http2PushMappings = new ArrayList<>(http2PushMap);
        }
        return this;
    }

    @Override
    public synchronized StaticHandler setEnableFSTuning(final boolean enableFSTuning) {
        this.tuning = enableFSTuning;
        if (!this.tuning) {
            this.resetTuning();
        }
        return this;
    }

    @Override
    public StaticHandler setMaxAvgServeTimeNs(final long maxAvgServeTimeNanoSeconds) {
        this.maxAvgServeTimeNanoSeconds = maxAvgServeTimeNanoSeconds;
        return this;
    }

    @Override
    public StaticHandler setSendVaryHeader(final boolean sendVaryHeader) {
        this.sendVaryHeader = sendVaryHeader;
        return this;
    }

    @Override
    public StaticHandler setDefaultContentEncoding(final String contentEncoding) {
        this.defaultContentEncoding = contentEncoding;
        return this;
    }

    private Map<String, CacheEntry> propsCache() {
        if (this.propsCache == null) {
            this.propsCache = new LRUCache<>(this.maxCacheSize);
        }
        return this.propsCache;
    }

    private Date parseDate(final String header) {
        try {
            return this.dateTimeFormatter.parse(header);
        } catch (final ParseException e) {
            throw new VertxException(e);
        }
    }

    private String getFile(final String path, final RoutingContext context) {
        final String file = this.webRoot + Utils.pathOffset(path, context);
        if (StaticHandlerImpl.log.isTraceEnabled()) {
            StaticHandlerImpl.log.trace("File to serve is " + file);
        }
        return file;
    }

    private void setRoot(final String webRoot) {
        Objects.requireNonNull(webRoot);
        if (!this.allowRootFileSystemAccess) {
            for (final File root : File.listRoots()) {
                if (webRoot.startsWith(root.getAbsolutePath())) {
                    throw new IllegalArgumentException("root cannot start with '" + root.getAbsolutePath() + "'");
                }
            }
        }
        this.webRoot = webRoot;
    }

    private void sendDirectoryListing(final String dir, final RoutingContext context) {
        final FileSystem fileSystem = context.vertx().fileSystem();
        final HttpServerRequest request = context.request();

        fileSystem.readDir(dir, asyncResult -> {
            if (asyncResult.failed()) {
                context.fail(asyncResult.cause());
            } else {

                String accept = request.headers().get("accept");
                if (accept == null) {
                    accept = "text/plain";
                }

                if (accept.contains("html")) {
                    String normalizedDir = context.normalisedPath();
                    if (!normalizedDir.endsWith("/")) {
                        normalizedDir += "/";
                    }

                    String file;
                    final StringBuilder files = new StringBuilder("<ul id=\"files\">");

                    final List<String> list = asyncResult.result();
                    Collections.sort(list);

                    for (final String s : list) {
                        file = s.substring(s.lastIndexOf(File.separatorChar) + 1);
                        // skip dot files
                        if (!this.includeHidden && file.charAt(0) == '.') {
                            continue;
                        }
                        files.append("<li><a href=\"");
                        files.append(normalizedDir);
                        files.append(file);
                        files.append("\" title=\"");
                        files.append(file);
                        files.append("\">");
                        files.append(file);
                        files.append("</a></li>");
                    }

                    files.append("</ul>");

                    // link to parent dir
                    int slashPos = 0;
                    for (int i = normalizedDir.length() - 2; i > 0; i--) {
                        if (normalizedDir.charAt(i) == '/') {
                            slashPos = i;
                            break;
                        }
                    }

                    final String parent = "<a href=\"" + normalizedDir.substring(0, slashPos + 1) + "\">..</a>";

                    request.response().putHeader("content-type", "text/html");
                    request.response().end(this.directoryTemplate(context.vertx()).replace("{directory}", normalizedDir).replace("{parent}", parent)
                            .replace("{files}", files.toString()));
                } else if (accept.contains("json")) {
                    String file;
                    final JsonArray json = new JsonArray();

                    for (final String s : asyncResult.result()) {
                        file = s.substring(s.lastIndexOf(File.separatorChar) + 1);
                        // skip dot files
                        if (!this.includeHidden && file.charAt(0) == '.') {
                            continue;
                        }
                        json.add(file);
                    }
                    request.response().putHeader("content-type", "application/json");
                    request.response().end(json.encode());
                } else {
                    String file;
                    final StringBuilder buffer = new StringBuilder();

                    for (final String s : asyncResult.result()) {
                        file = s.substring(s.lastIndexOf(File.separatorChar) + 1);
                        // skip dot files
                        if (!this.includeHidden && file.charAt(0) == '.') {
                            continue;
                        }
                        buffer.append(file);
                        buffer.append('\n');
                    }

                    request.response().putHeader("content-type", "text/plain");
                    request.response().end(buffer.toString());
                }
            }
        });
    }

    // TODO make this static and use Java8 DateTimeFormatter
    private final class CacheEntry {
        final FileProps props;
        long createDate;

        private CacheEntry(final FileProps props, final long createDate) {
            this.props = props;
            this.createDate = createDate;
        }

        // return true if there are conditional headers present and they match what is in the entry
        boolean shouldUseCached(final HttpServerRequest request) {
            final String ifModifiedSince = request.headers().get("if-modified-since");
            if (ifModifiedSince == null) {
                // Not a conditional request
                return false;
            }
            final Date ifModifiedSinceDate = net.akehurst.application.framework.technology.gui.vertx.StaticHandlerImpl.this.parseDate(ifModifiedSince);
            final boolean modifiedSince = Utils.secondsFactor(this.props.lastModifiedTime()) > ifModifiedSinceDate.getTime();
            return !modifiedSince;
        }

        boolean isOutOfDate() {
            return System.currentTimeMillis()
                    - this.createDate > net.akehurst.application.framework.technology.gui.vertx.StaticHandlerImpl.this.cacheEntryTimeout;
        }

    }
}
