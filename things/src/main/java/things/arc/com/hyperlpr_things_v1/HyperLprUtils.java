package things.arc.com.hyperlpr_things_v1;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;


public class HyperLprUtils {

    public static final String mainFolderPath = Environment.getExternalStorageDirectory().toString()+ File.separator+"HyperLpr_Thing";
    public static final String modelFolderPath = mainFolderPath + File.separator + "model";
    public static final String pictureFolderPath = mainFolderPath + File.separator + "picture";
    public static final String testCarPlatePictureFolderPath = mainFolderPath+File.separator+"test_picture_car_plate";
    public static final String plateRecognizeJSONFoldrPath = mainFolderPath+File.separator+"resultJSON";
    public static final String resultJSONName = "plate_result.json";

    public static final String characterRecognizationCaffeModel = "CharacterRecognization.caffemodel";
    public static final String characterRecognizationPrototxt = "CharacterRecognization.prototxt";
    public static final String horizonalFinemappingCaffeModel = "HorizonalFinemapping.caffemodel";
    public static final String horizonalFinemappingPrototxt = "HorizonalFinemapping.prototxt";
    public static final String segmentationCaffeModel = "Segmentation.caffemodel";
    public static final String segmentationPrototxt = "Segmentation.prototxt";
    public static final String cascadeXML = "cascade.xml";

    /**
     * 在SD卡创建文件夹
     */
    public static void creatFolders(){

        File file1 = new File(mainFolderPath);
        if (file1.exists()){
            return;
        }
        file1.mkdir();

        File file2 = new File(modelFolderPath);
        file2.mkdir();

        File file3 = new File(pictureFolderPath);
        file3.mkdir();

        File file4 = new File(testCarPlatePictureFolderPath);
        file4.mkdir();

        File file5 = new File(plateRecognizeJSONFoldrPath);
        file5.mkdir();

    }

    /**
     * 将Assets中的模型文件复制到SD卡
     * @param context
     */
    public static void copyModelToSdCard(Context context){

        String[] modelFileNameList = {
                characterRecognizationCaffeModel,
                characterRecognizationPrototxt,
                horizonalFinemappingCaffeModel,
                horizonalFinemappingPrototxt,
                segmentationCaffeModel,
                segmentationPrototxt,
                cascadeXML
        };
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        byte[] bytes = new byte[1024];
        int byteCount = 0;
        File file = null;
        for (String fileName:modelFileNameList){
            try {
                inputStream = assetManager.open(fileName);
                //创建文件
                file = new File(modelFolderPath+File.separator+fileName);
                file.createNewFile();
                outputStream = new FileOutputStream(file);
                while ((byteCount = inputStream.read(bytes))!=-1){
                    outputStream.write(bytes,0,byteCount);
                }

            }catch (IOException e){
                e.printStackTrace();
            }
        }

        try {
            if (inputStream!=null){
                inputStream.close();
            }
            if (outputStream!=null){
                outputStream.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }


    }

    /**
     * 将Assets中的图片复制到SD卡
     * @param context
     */
    public static void copyTestCarPlatePictureToSdCard(Context context){
        String[] pictureNameList = {
                "0.jpg",
                "1.jpg",
                "2.jpg",
                "3.jpg",
                "4.jpg",
                "5.jpg",
        };

        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        File file = null;
        byte[] bytes = new byte[1024];
        int len = 0;
        for (String name:pictureNameList){
            try {
                inputStream = assetManager.open(name);
                outputStream = new FileOutputStream(testCarPlatePictureFolderPath+File.separator+name);

                while ((len = inputStream.read(bytes))!=-1){
                    outputStream.write(bytes,0,len);
                }

            }catch (IOException e){
                e.printStackTrace();
            }

        }

        try {
            if (inputStream!=null){
                inputStream.close();
            }if (outputStream!=null){
                outputStream.close();
            }

        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void creatJSONFile(String plateFullStr,String plateNum,String province,String backgroundColor,String textColor){

        JSONObject jsonObjectMain = new JSONObject();       //JSON主对象
        JSONObject jsonObjectChild = new JSONObject();      //JSON子对象
        try {

            jsonObjectChild.put("num",plateNum);
            jsonObjectChild.put("province",province);
            jsonObjectChild.put("background_color",backgroundColor);
            jsonObjectChild.put("text_color",textColor);

        }catch (JSONException e){
            e.printStackTrace();
        }
        try {
            jsonObjectMain.put("plate_str",plateFullStr);
            jsonObjectMain.put("plate_info",jsonObjectChild);

        }catch (JSONException e){
            e.printStackTrace();
        }
        //转换为json字符串
        String jsonStr = jsonObjectMain.toString();
        File file = new File(plateRecognizeJSONFoldrPath+File.separator+resultJSONName);
        if (file.exists()){
            file.delete();
        }

        //重新创建
        try {
            file.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }

        Writer writer = null;
        try {
            //写入文件
            writer= new FileWriter(file);
            writer.write(jsonStr);
            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }


    public static String creatJSONStr(String plateFullStr,String plateNum,String province,String backgroundColor,String textColor){

        JSONObject jsonObjectMain = new JSONObject();       //JSON主对象
        JSONObject jsonObjectChild = new JSONObject();      //JSON子对象
        try {

            jsonObjectChild.put("num",plateNum);
            jsonObjectChild.put("province",province);
            jsonObjectChild.put("background_color",backgroundColor);
            jsonObjectChild.put("text_color",textColor);

        }catch (JSONException e){
            e.printStackTrace();
        }
        try {
            jsonObjectMain.put("plate_str",plateFullStr);
            jsonObjectMain.put("plate_info",jsonObjectChild);

        }catch (JSONException e){
            e.printStackTrace();
        }
        //转换为json字符串
        return jsonObjectMain.toString();

    }




}
