package bgu.spl.net.api.Message.ClientToServer;

import bgu.spl.net.api.Message.Message;

public class PM extends Message {

    // Fields
    private final String userName;
    private String content;
    private final String[] wordsToFilter = {"Trump", "War"};

    // Constructor
    public PM(String userName, String content) {
        super((short) 6);
        this.userName = userName;
        this.content = content;
        filterContent();
    }

    // Getters
    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    // Filter words that forbidden and replace them with the string "filtered"
    public void filterContent() {
        String space = " ";
        content = space + content + space;
        for (int i = 0; i < wordsToFilter.length; i++) {
            content = content.replaceAll(" " + wordsToFilter[i] + "?", " <filtered>");
            content = content.replaceAll(" " + wordsToFilter[i] + ".", " <filtered>");
            content = content.replaceAll(" " + wordsToFilter[i] + ",", " <filtered>");
            content = content.replaceAll(" " + wordsToFilter[i] + " ", " <filtered> ");
            content = content.replaceAll(" " + wordsToFilter[i] + "!", " <filtered>");
        }
        content = content.substring(1, content.length() - 1);
    }

}
