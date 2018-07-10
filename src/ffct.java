
import java.io.*;


public class ffct {

    static private int threads = 4;

    static public void main(String[] args) {
        try {
            threads = Integer.parseInt(args[0]);
        } catch (Exception e) {
            threads = 4;
        }
        listFiles(".");
    }

    private static void listFiles(String path) {
        File directory = new File(path);
        for (File f : directory.listFiles()) {
            if (f.isFile()) {
                convert(f.getAbsolutePath());
            } else if (f.isDirectory()) {
                listFiles(f.getPath());
            }
        }
    }

    private static void convert(String file) {
        if (file.endsWith(".265.mp4") || file.endsWith(".class")) {
            System.out.println(file + " has skipped.");
            return;
        }

        String cmds[] = {
                "ffmpeg", "-threads", threads + "", "-i", file, "-y", "-vcodec", "libx265", "-acodec", "mp3",
                "-f", "mp4", file + ".265.mp4"
        };

        StringBuilder sb = new StringBuilder();
        for (String s : cmds) sb.append(s).append(" ");
        System.out.println(new String(sb));

        try {
            Process p = Runtime.getRuntime().exec(cmds);
            new StreamGobbler(p.getInputStream(), StreamGobbler.INFO).start();
            new StreamGobbler(p.getErrorStream(), StreamGobbler.ERROR).start();
            p.waitFor();
            new File(file).delete();
        } catch (Exception e) {
            e.printStackTrace();
            File f = new File(file + ".265.mp4");
            f.delete();
        }
    }

    static private class StreamGobbler extends Thread {
        private InputStream is;
        private int type;

        public static final int INFO = 0;
        public static final int ERROR = 1;

        public StreamGobbler(InputStream is, int type) {
            this.is = is;
            this.type = type;
        }

        @Override
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is, "gbk");
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (type == INFO) {
                        System.out.println(line);
                    } else if (type == ERROR) {
                        System.out.println(line);
                    }
                }
                is.close();
                isr.close();
                br.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
