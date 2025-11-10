package neko.nekoAlert;

public class MessageEntry {
    private String content;
    private int interval;
    
    public MessageEntry() {
        // 默认构造函数
    }
    
    public MessageEntry(String content, int interval) {
        this.content = content;
        this.interval = interval;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public int getInterval() {
        return interval;
    }
    
    public void setInterval(int interval) {
        this.interval = interval;
    }
}