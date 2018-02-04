package net.akehurst.application.framework.technology.interfaceGui.data.editor;

public interface IGuiLanguageService {

    String getIdentity();

    void load();

    void revert();

    void save();

    /**
     *
     * @return JSON String of the parseTree
     */
    String update(String text);

    void assist();

    void validate();

    void hover();

    void highlight();

    void occurrences();

    void format();
}
