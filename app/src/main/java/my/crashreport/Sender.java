package my.crashreport;

/**
 * Created by Ilian Georgiev.
 */
public interface Sender {

    /**
     * Send the report to a repository.
     * @param content The crash report content
     * @return {@code true} is the report was sent successfully and {@code false} if not.
     */
    boolean send(String content);
}
