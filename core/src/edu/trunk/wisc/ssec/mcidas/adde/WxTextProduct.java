package edu.wisc.ssec.mcidas.adde;

import edu.wisc.ssec.mcidas.McIDASUtil;
import java.util.Date;

public class WxTextProduct {
       String source = "";
       int numBytes = 0;
       int location = 0;
       int day = 0;
       int time = 0;
       String wmoId = "";
       String wmoStn = "";
       String apro = "";
       String astn = "";
       String text = "";
       Date date = new Date();

       public WxTextProduct(byte[] header) {
           int[] values = McIDASUtil.bytesToIntegerArray(header,0,13);
           source = McIDASUtil.intBitsToString(values[0]);
           numBytes = values[1];
           location = values[2];
           day = values[10];
           time = values[3];
           String wmoBase = McIDASUtil.intBitsToString(values[4]);
           int wmoNum = values[5];
           wmoId = wmoBase + ((wmoNum < 10) ? "0" : "") + wmoNum;
           wmoStn = McIDASUtil.intBitsToString(values[6]);
           apro = McIDASUtil.intBitsToString(values[7]);
           astn = McIDASUtil.intBitsToString(values[8]);
           date = new Date(McIDASUtil.mcDayTimeToSecs(day,time)*1000);
        }

        public void addText(String newText) {
           text = text + newText;
        }

        public void setText(String newText) {
           text = newText;
        }

        public String getText() {
           return text;
        }

        public String getSource() {
           return source;
        }

        public int getDay() {
           return day;
        }

        public int getTime() {
           return time;
        }

        public String getWmo() {
           return wmoId;
        }

        public String getWstn() {
           return wmoStn;
        }

        public String getApro() {
           return apro;
        }

        public String getAstn() {
           return astn;
        }

        public Date getDate() {
           return date;
        }

    }
