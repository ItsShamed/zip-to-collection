package me.Shamed.osu.zip2osdb.utils;

import com.google.common.io.LittleEndianDataOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class BinaryEditing {


    // Adapted code from AOSP
    public static byte[] getUnsignedLeb128(int value) throws IOException {
        int remaining = value >>> 7;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LittleEndianDataOutputStream ledos = new LittleEndianDataOutputStream(baos);

        while (remaining!=0){
            ledos.writeByte((byte)(value & 0x7f | 0x80));
            value = remaining;
            remaining >>>= 7;
        }

        ledos.writeByte((byte) (value & 0x7f));
        ledos.flush();
        ledos.close();
        baos.flush();
        baos.close();

        return baos.toByteArray();

    }

    public static void writeCSUTF(LittleEndianDataOutputStream outputStream, String string) throws IOException{
        outputStream.write(BinaryEditing.getUnsignedLeb128(string.length()));
        outputStream.write(string.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Convert Date to Microsoft OLE Automation - OADate type
     * @param date
     * @return
     * @throws ParseException
     */
    @SuppressWarnings("Deprecated")
    public static double convertToOADate(Date date) throws ParseException {
        double oaDate;
        SimpleDateFormat myFormat = new SimpleDateFormat("dd MM yyyy");
        Date baseDate = myFormat.parse("30 12 1899");
        Long days = TimeUnit.DAYS.convert(date.getTime() - baseDate.getTime(), TimeUnit.MILLISECONDS);

        oaDate = (double) days + ((double) date.getHours() / 24) + ((double) date.getMinutes() / (60 * 24)) + ((double)                     date.getSeconds() / (60 * 24 * 60));
        return oaDate;
    }

}
