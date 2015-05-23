package dynamicloader;

import java.lang.reflect.Field;

/**
 * Created by luliang on 5/23/15.
 */
public class ReflectionUtils {

    public static Field getField(Object classObject, String filedName) throws IllegalAccessException {
        Field field = null;
        Class<?> tClass = classObject.getClass();
        while (field == null) {
            try {
                field =  tClass.getDeclaredField(filedName);
                field.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                if (tClass.getSuperclass() != null) {
                    tClass = tClass.getSuperclass();
                }else{
                    break;
                }
            }
        }

     return   field;
    }

    public static Object getFieldObject(Object classObject, String filedName) throws IllegalAccessException {
        Field field = getField(classObject, filedName);
        return field.get(classObject);
    }


    public static void setField(Object obj, String fieldName, Object value) throws IllegalAccessException {
        Field filed = getField(obj, fieldName);
        filed.set(obj, value);
    }


}
