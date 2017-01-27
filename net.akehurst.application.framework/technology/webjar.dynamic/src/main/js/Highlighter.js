
define(['orion/editor/eventTarget'], function(mEventTarget) {
	
	/**
	 * The SendStyle event payload conforms to the orion.editor.AsyncStyler API.
	 */
	function Highlighter() {
		this.annotationModel = null
		this.initialize();
	}

	Highlighter.prototype = {
		
		initialize: function() {
			var self = this;
			this.listener = {
				onDestroy: function(e) {
					self._onDestroy(e);
				},
				onHighlight: function(e) {
					self.sendStyle(e);
				}
			};
		},
		destroy: function() {
			this.listener = null;
		},
		setAnnotationModel : function(editorViewer) {
			this.annotationModel = editorViewer.editor.getAnnotationModel()
		},
		sendStyle: function(e) {
			let start = e.start
			let end = e.end;
			var styles = {};
			styles[0] = {
			  errors : [],
			  ranges : [{ start:e.start, end:e.end, style:e.rangeStyle }]
			
			}
			
			var event = {
				type: "StyleReady",
				lineStyles: styles
			};
			this.dispatchEvent(event);

			console.debug("Fired " + JSON.stringify(event));
		},
		createAnnotation : function(start, length, title, cssClass) {
			return {
			    title : title,
		    	start : start,
		    	end : start + length,
		    	//html : "<p>Test Html Annotation</p>",
		    	//lineStyle : { attributes:{}, style:{}, styleClass:'testLineStyleClass', tagName:'span' },
		    	//overviewStyle: { attributes:{}, style:{}, styleClass:'testOverviewStyleClass', tagName:'span' },
		    	rangeStyle : { attributes:{}, style:{}, styleClass: cssClass, tagName:'span' },
		    	//style : { attributes:{}, style:{}, styleClass:'cssClass', tagName:'span' },
		    	type : 'orion.annotation.error' //"parseTree"
		    }
		},
		doNode : function(node) {
			if (node.isPattern) {
				return //can't use pattern name as a css-class
			}
			let ann = this.createAnnotation(node.start, node.length, node.name, node.name)
			this.annotationModel.addAnnotation(ann)
			this.sendStyle(ann)
			if (null != node.children) {
				for(let i=0, len=node.children.length; i < len; i++) {
					let child = node.children[i]
					this.doNode(child)
				}
			}
		},
		update : function(parseTree) {
			this.annotationModel.removeAnnotations('parseTree')
			this.doNode(parseTree)
			
		}
	};
	mEventTarget.EventTarget.addMixin(Highlighter.prototype);

	return {
		Highlighter: Highlighter
	};
});