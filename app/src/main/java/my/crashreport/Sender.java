package my.crashreport;

/**
 * Created by Ilian Georgiev.
 */
public interface Sender {
    interface Listener {
        void onReportSend(String filename);
    }

    void send(String filename, String content, Listener listener);
}
