package teamdoppelganger.smarterbus.util.common;

import java.io.File;

import android.provider.SyncStateContract.Constants;


public class FileManager {


    public static void removeDIR(String source) {
        File[] listFile = new File(source).listFiles();
        try {
            if (listFile.length > 0) {
                for (int i = 0; i < listFile.length; i++) {
                    if (listFile[i].isFile()) {
                        listFile[i].delete();
                    } else {
                        removeDIR(listFile[i].getPath());
                    }
                    listFile[i].delete();
                }
            }
        } catch (Exception e) {
            System.exit(-1);
        }
    }

    public static void moveInDIRFile(String source) {
        File[] listFile = new File(source).listFiles();
        try {
            if (listFile.length > 0) {
                for (int i = 0; i < listFile.length; i++) {
                    if (listFile[i].isFile()) {

                        String[] fileName = listFile[i].getPath().split("\\/");
                        fileMove(listFile[i].getPath(),
                                teamdoppelganger.smarterbus.common.Constants.DOWNLOAD_PATH + File.separator
                                        + fileName[fileName.length - 1]);
                    }
                }
            }
        } catch (Exception e) {
            System.exit(-1);
        }
    }

    public static void fileMove(String inFileName, String outFileName) {

        File file = new File(inFileName);
        File file2 = new File(outFileName);// 이동

        if (file.exists()) {
            file.renameTo(file2); // 변경
        }

        fileDelete(inFileName);

    }

    public static void fileDelete(String deleteFileName) {
        File I = new File(deleteFileName);
        I.delete();
    }

}
