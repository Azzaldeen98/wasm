package com.example.wasmapplication.core.local_storage;

import android.content.Context;

import com.example.wasmapplication.core.constant.Constants;


public class LanguageInfo {

        private String code;
        private int index;



        public LanguageInfo(String code, int index) {
            this.code = code;
            this.index = index;
        }

        public String getCode() {
            return code;
        }

        public int getIndex() {
            return index;
        }

    public static  String  getCurrentAppLanguage(Context context){
       Object lang = ExternalStorage.getValue(context, Constants.LANGUAGE);
       return (lang!=null)?lang.toString():"";
    }
    public static  void  setStorageSelectedLanguage(Context context, String langCode, int langIndex){
        ExternalStorage.storage(context, Constants.LANGUAGE,langCode);
        ExternalStorage.storage(context,Constants.LANGUAGE_INDEX,langIndex);
    }

    public static LanguageInfo getStorageSelectedLanguage(Context context){

        if(ExternalStorage.existing(context,Constants.LANGUAGE) && ExternalStorage.existing(context,Constants.LANGUAGE_INDEX)) {

            Object code = ExternalStorage.getValue(context, Constants.LANGUAGE);
            if(code==null) return null;
            int index = ExternalStorage.getIntValue(context,Constants.LANGUAGE_INDEX);
            LanguageInfo languageInfo = new LanguageInfo(code.toString().trim(),index);
            return languageInfo;
        }
        return null;
    }

    public static void removeStorageSelectedLanguage(Context context){

        ExternalStorage.remove(context,Constants.LANGUAGE);
        ExternalStorage.remove(context,Constants.LANGUAGE_INDEX);
    }


}
