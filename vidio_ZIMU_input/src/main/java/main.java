import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import com.benjaminwan.ocrlibrary.OcrResult;
import io.github.mymonstercat.Model;
import io.github.mymonstercat.ocr.InferenceEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @time: 2024/3/29 3:09
 * @description:
 */

public class main {
    public static void main(String[] args) {
        String filepath = "C:\\Users\\Administrator\\Desktop\\新建文件夹";
        String ffmpeg = "D:\\Developmen\\ffmpeg-master-latest-win64-gpl-shared\\bin\\ffmpeg.exe";
        File file = new File(filepath);
        Runtime runtime = Runtime.getRuntime();
        for (File listFile : file.listFiles()) {
            String fileExtension = getFileExtension(listFile);
            if (fileExtension.equals("mp4")) {
                String jpg = listFile.getPath().replaceAll(listFile.getName(), "jpg");
                new File(jpg).mkdir();
                String dom = ffmpeg + " -i " + listFile.getPath() + " -vf \"fps=1,crop=iw:ih*0.11:iw*0.44:ih*0.44\"" +
                        " " +
                        jpg + "\\%d.jpg";
                System.out.println(dom);
                try {
                    // 执行命令
                    Process process = runtime.exec(dom);
                    // 创建流来读取FFmpeg的输出和错误信息（非必需，但有助于调试）
                    InputStream stdout = process.getInputStream();
                    InputStream stderr = process.getErrorStream();
                    // 新建线程分别读取输出和错误信息
                    new Thread(() -> readStream(stdout)).start();
                    new Thread(() -> readStream(stderr)).start();
                    // 等待进程执行完成
                    int exitCode = process.waitFor();
                    // 检查退出码
                    if (exitCode == 0) {
                        System.out.println("FFmpeg命令执行成功");
                    } else {
                        System.err.println("FFmpeg命令执行失败，退出码: " + exitCode);
                    }
                    LinkedList<String> list = ocr_text("C:\\Users\\Administrator\\Desktop\\新建文件夹\\jpg");
                    LinkedList<String> list1 = textfilter1(list);
                    LinkedList<String> list2 = textfilter1(list1);
                    StringBuilder text = new StringBuilder();
                    for (String s : list2) {
                        text.append(s).append("\n");
                    }
                    String s = listFile.getPath().replaceAll(listFile.getName(), "text.txt");
                    FileWriter writer = new FileWriter(s);
                    writer.write(text.toString());
                    FileUtil.del(jpg);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void readStream(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static LinkedList<String> ocr_text(String imgpath) {
        InferenceEngine engine = InferenceEngine.getInstance(Model.ONNX_PPOCR_V3);
        LinkedList<String> texts = new LinkedList<>();


        File file = new File(imgpath);
        File[] files = sortFilesByNumberInName(file);
        for (File listFile : files) {
            System.out.println(listFile.getName());
            OcrResult ocrResult = engine.runOcr(listFile.getPath());
            System.out.println(ocrResult.getStrRes().trim());
            texts.add(ocrResult.getStrRes().trim());
        }
        return texts;
    }

    public static File[] sortFilesByNumberInName(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return new File[0]; // 如果目录为空或不存在，返回空数组
        }

        Arrays.sort(files, new Comparator<File>() {
            private final Pattern pattern = Pattern.compile("(\\d+)");

            @Override
            public int compare(File f1, File f2) {
                Matcher matcher1 = pattern.matcher(f1.getName());
                Matcher matcher2 = pattern.matcher(f2.getName());
                if (matcher1.find() && matcher2.find()) {
                    return Integer.compare(Integer.parseInt(matcher1.group(1)),
                            Integer.parseInt(matcher2.group(1)));
                }
                return f1.getName().compareTo(f2.getName());
            }
        });

        return files;
    }

    public static String getFileExtension(File file) {
        if (file == null || !file.exists()) {
            return "";
        }
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        } else {
            return "";
        }
    }

    public static LinkedList<String> textfilter1(LinkedList<String> texts) {
        for (int i = 1; i < texts.size(); i++) {

            if (texts.get(i).equals(texts.get(i - 1)) | texts.get(i).equals("") | texts.get(i) == null) {
                texts.remove(i);
            }
        }
        return texts;
    }


}
