package org.geotools.otherFunction;
import jxl.Workbook;
import jxl.format.*;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.write.*;
import jxl.write.Colour;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public class transformToExcel {
	  public void writeExcel(String path, List<String[]> list, String sheet,String[] title) {
	        try {
	            // 创建Excel工作薄
	            WritableWorkbook wwb = null;
	            // 新建立一个jxl文件
	            OutputStream os = new FileOutputStream(path);
	            wwb = Workbook.createWorkbook(os);
	            // 添加第一个工作表并设置第一个Sheet的名字
	            WritableSheet sheets = wwb.createSheet(sheet, 1);
	            Label label;
	            for (int i = 0; i < title.length; i++) {
	                // Label(x,y,z) 代表单元格的第x+1列，第y+1行, 内容z
	                // 在Label对象的子对象中指明单元格的位置和内容
//	          label = new Label(i, 0, title[i]);
	                label = new Label(i, 0, title[i], getHeader());
	                //设置列宽
	                sheets.setColumnView(i, 20);
//	          sheets.setColumnView(4, 100);
	                // 将定义好的单元格添加到工作表中
	                sheets.addCell(label);
	            }

	            //设置单元格属性
	            WritableCellFormat wc = new WritableCellFormat();
	            // 设置居中
	            wc.setAlignment(Alignment.CENTRE);
	            // 设置边框线
	            wc.setBorder(Border.ALL, BorderLineStyle.THIN);

	            for (int i = 0; i < list.size(); i++) {
	                String[] arrData = list.get(i);
	                for (int j = 0; j < arrData.length; j++) {
	                    //向特定单元格写入数据
	                    //sheets.setColumnView(j, 20);
	                    label = new Label( i,j+1 , arrData[j], wc);
	                    sheets.addCell(label);
	                }
	            }
	            // 写入数据
	            wwb.write();
	            // 关闭文件
	            wwb.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	            return ;
	        }
	    }
	    public static WritableCellFormat getHeader() {
	        // 定义字体
	        WritableFont font = new WritableFont(WritableFont.TIMES, 10,
	                WritableFont.BOLD);
	        try {
	            // 黑色字体
	            font.setColour(jxl.format.Colour.BLACK);
	        } catch (WriteException e1) {
	            e1.printStackTrace();
	        }
	        WritableCellFormat format = new WritableCellFormat(font);
	        try {
	            // 左右居中
	            format.setAlignment(jxl.format.Alignment.CENTRE);
	            // 上下居中
	            format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
	            // 黑色边框
	            format.setBorder(Border.ALL, BorderLineStyle.THIN, jxl.format.Colour.BLACK);
	            // 黄色背景
	            format.setBackground(jxl.format.Colour.YELLOW);
	        } catch (WriteException e) {
	            e.printStackTrace();
	        }
	        return format;
	    }
}
