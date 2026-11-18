package dev.projectorsoft.tvsettings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Stored provider values. The profile markers do not identify the active MediaTek input. */
final class PicturePanelState {
    interface Reader {String read(String key);}
    static final String MODE="picture_mode";
    static final String TEMPERATURE="picture_color_temperature";
    static final String HDR="picture_list_hdr";
    static final String[] KEYS={
        MODE,"picture_brightness","picture_contrast","picture_saturation","picture_sharpness",
        "picture_hue",TEMPERATURE,"picture_red_gain","picture_green_gain","picture_blue_gain",
        HDR,"tv_picture_advance_video_mjc_effect"
    };
    static final String[] TITLES={
        "Режим изображения","Яркость изображения","Контрастность","Насыщенность","Резкость",
        "Оттенок","Цветовая температура","Красный канал","Зелёный канал","Синий канал","HDR","MEMC"
    };

    private PicturePanelState(){}

    static void observe(Consumer<String> register, BiConsumer<String,RuntimeException> onFailure){
        for(String key:KEYS){
            try{register.accept(key);}
            catch(RuntimeException error){onFailure.accept(key,error);}
        }
    }

    static Map<String,Integer> read(Reader reader){
        String mode=reader.read(MODE);
        String temperature=reader.read(TEMPERATURE);
        String hdr=reader.read(HDR);
        Map<String,Integer> values=new LinkedHashMap<>();
        for(String key:KEYS){
            String value=MODE.equals(key)?mode:TEMPERATURE.equals(key)?temperature:HDR.equals(key)?hdr:reader.read(key);
            try {
                if(value!=null)values.put(key,Integer.valueOf(value));
            }catch(NumberFormatException ignored){}
        }
        if(!Objects.equals(mode,reader.read(MODE)) || !Objects.equals(temperature,reader.read(TEMPERATURE))
                || !Objects.equals(hdr,reader.read(HDR)))return null;
        return values;
    }
}
