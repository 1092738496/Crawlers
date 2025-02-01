/**
 * @time: 2024/3/29 4:07
 * @description:
 */

public class test {
    /**
     * 将帧数转换为时分秒格式的时间
     *
     * @param frameNumber 当前帧编号（从0开始）
     * @param fps         视频的帧率（每秒帧数）
     * @return 格式化的时间字符串（HH:mm:ss）
     */
    public static String convertFrameToTime(int frameNumber, int fps) {
        // 计算总秒数
        int totalSeconds = frameNumber / fps;

        // 计算小时、分钟和秒
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        // 格式化为 HH:mm:ss
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static void main(String[] args) {
        // 示例调用
        int frameNumber = 1222; // 示例：第7201帧
        int fps = 2; // 示例：视频帧率为24 FPS

        String formattedTime = convertFrameToTime(frameNumber, fps);
        System.out.println("帧 " + frameNumber + " 对应的时间: " + formattedTime);
    }
}
