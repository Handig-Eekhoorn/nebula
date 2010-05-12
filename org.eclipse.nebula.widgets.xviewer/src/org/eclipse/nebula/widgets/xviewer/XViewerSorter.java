/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.xviewer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn.SortDataType;
import org.eclipse.nebula.widgets.xviewer.util.internal.XViewerLog;

/**
 * XTreeSorter is equipped to: 1) Sort columns forward and backward by re-selecting the column 2) Sort by multiple
 * columns
 * 
 * @author Donald G. Dunne
 */
public class XViewerSorter extends ViewerSorter {
   private final XViewer treeViewer;
   private final static SimpleDateFormat format10 = new SimpleDateFormat("MM/dd/yyyy");
   private final static SimpleDateFormat defaultFormat = new SimpleDateFormat();

   public XViewerSorter(XViewer treeViewer) {
      super();
      this.treeViewer = treeViewer;
   }

   @SuppressWarnings("unchecked")
   public int compare(Viewer viewer, Object o1, Object o2, int sortXColIndex) {
      if (treeViewer.getCustomizeMgr().isLoading()) {
         return 0;
      }
      List<XViewerColumn> sortXCols = treeViewer.getCustomizeMgr().getSortXCols();
      if (sortXCols == null || sortXCols.size() == 0) {
         return 0;
      }
      XViewerColumn sortXCol = sortXCols.get(sortXColIndex);
      try {
         int columnNum = treeViewer.getCustomizeMgr().getColumnNumFromXViewerColumn(sortXCol);

         String o1Str = treeViewer.getColumnText(o1, columnNum);
         String o2Str = treeViewer.getColumnText(o2, columnNum);

         // System.out.println("sortForward.get(columnNum) *" +
         // sortXCol.isSortForward() + "*");
         int compareInt = 0;
         if (o1Str == null) {
            compareInt = -1;
         } else if (o2Str == null) {
            compareInt = 1;
         } else if (sortXCol.getSortDataType() == SortDataType.Date) {
            compareInt = getCompareForDate(o1Str, o2Str);
         } else if (sortXCol.getSortDataType() == SortDataType.Percent) {
            compareInt = getCompareForPercent(o1Str, o2Str);
         } else if (sortXCol.getSortDataType() == SortDataType.Float) {
            compareInt = getCompareForFloat(o1Str, o2Str);
         } else if (sortXCol.getSortDataType() == SortDataType.Integer) {
            compareInt = getCompareForInteger(o1Str, o2Str);
         } else if (sortXCol.getSortDataType() == SortDataType.Paragraph_Number) {
            compareInt = paragraphNumberCompare(o1Str, o2Str);
         } else {
            compareInt = getComparator().compare(o1Str, o2Str);
         }

         int compareOnDir = getCompareBasedOnDirection(sortXCol, compareInt, viewer, o1, o2, sortXColIndex);

         //         System.out.println(String.format("Dir [%d] Compare [%d] Str1 [%s] Str2 [%s] Column [%s]", compareInt,
         //               compareOnDir, o1Str, o2Str, sortXCol));

         return compareOnDir;

      } catch (Exception ex) {
         XViewerLog.log(Activator.class, Level.SEVERE, ex);
      }
      return 0;
   }

   @Override
   public int compare(Viewer viewer, Object o1, Object o2) {
      return compare(viewer, o1, o2, 0);
   }

   public int paragraphNumberCompare(String s1, String s2) {
      StringTokenizer st1 = new StringTokenizer(s1, ".");
      StringTokenizer st2 = new StringTokenizer(s2, ".");
      while (st1.hasMoreTokens() && st2.hasMoreTokens()) {
         String t1 = st1.nextToken();
         String t2 = st2.nextToken();

         int result;
         try {
            Integer i1 = new Integer(t1);
            Integer i2 = new Integer(t2);
            result = i1.compareTo(i2);
         } catch (NumberFormatException e) {
            result = t1.compareTo(t2);
         }
         if (result != 0) {
            return result;
         }
      }

      return s1.compareTo(s2);
   }

   public int getCompareBasedOnDirection(XViewerColumn sortXCol, int compareInt, Viewer viewer, Object o1, Object o2, int sortXColIndex) {
      List<XViewerColumn> sortXCols = treeViewer.getCustomizeMgr().getSortXCols();
      int returnInt = (sortXCol.isSortForward() ? 1 : -1) * compareInt;
      // System.out.println("returnInt *" + returnInt + "*");
      if (returnInt == 0 && sortXCols.size() > sortXColIndex + 1) {
         returnInt = compare(viewer, o1, o2, (sortXColIndex + 1));
      }
      return returnInt;
   }

   public int getCompareForFloat(String float1, String float2) {
      double float1Float = 0;
      boolean float1Exception = false;
      try {
         float1Float = new Double(float1).doubleValue();
      } catch (NumberFormatException ex) {
         float1Exception = true;
      }
      double float2Float = 0;
      boolean float2Exception = false;
      try {
         float2Float = new Double(float2).doubleValue();
      } catch (NumberFormatException ex) {
         float2Exception = true;
      }
      int toReturn = 0;
      if (float1Exception && float2Exception)
         toReturn = getComparator().compare(float1, float2);
      else if (float1Exception && !float2Exception)
         toReturn = -1;
      else if (!float1Exception && float2Exception)
         toReturn = 1;
      else
         toReturn = getCompareForFloat(float1Float, float2Float);
      return toReturn;
   }

   public int getCompareForInteger(String int1, String int2) {
      int int1Integer = 0;
      boolean int1Exception = false;
      try {
         int1Integer = new Integer(int1).intValue();
      } catch (NumberFormatException ex) {
         int1Exception = true;
      }
      int int2Integer = 0;
      boolean int2Exception = false;
      try {
         int2Integer = new Integer(int2).intValue();
      } catch (NumberFormatException ex) {
         int2Exception = true;
      }
      int toReturn = 0;
      if (int1Exception && int2Exception)
         toReturn = getComparator().compare(int1, int2);
      else if (int1Exception && !int2Exception)
         toReturn = -1;
      else if (!int1Exception && int2Exception)
         toReturn = 1;
      else
         toReturn = getCompareForInteger(int1Integer, int2Integer);

      return toReturn;

   }

   public static int getCompareForFloat(double float1, double float2) {
      if (float1 == float2) {
         return 0;
      } else if (float1 < float2) {
         return -1;
      } else if (float2 < float1) {
         return 1;
      } else {
         return 0;
      }
   }

   public static int getCompareForInteger(int int1, int int2) {
      if (int1 == int2) {
         return 0;
      } else if (int1 < int2) {
         return -1;
      } else if (int2 < int1) {
         return 1;
      } else {
         return 0;
      }
   }

   public int getCompareForDate(String date1, String date2) {
      if (date1.trim().equals("") && date2.trim().equals("")) {
         return 0;
      }
      if (date1.trim().equals("")) {
         return -1;
      }
      if (date2.trim().equals("")) {
         return 1;
      }
      DateFormat format;
      if (date1.length() == 10) {
         format = format10;
      } else {
         format = defaultFormat;
      }
      Date date1Date = null;
      try {
         date1Date = format.parse(date1);
      } catch (ParseException ex) {
         try {
            date1Date = defaultFormat.parse(date1);
         } catch (ParseException ex2) {
            XViewerLog.log(Activator.class, Level.SEVERE, ex2);
            return 0;
         }
      }
      Date date2Date = null;
      try {
         date2Date = format.parse(date2);
      } catch (ParseException ex) {
         try {
            date2Date = SimpleDateFormat.getInstance().parse(date2);
         } catch (ParseException ex2) {
            XViewerLog.log(Activator.class, Level.SEVERE, ex2);
            return 0;
         }
      }
      return getCompareForDate(date1Date, date2Date);
   }

   public int getCompareForDate(Date date1, Date date2) {
      return date1.compareTo(date2);
   }

   public int getCompareForPercent(String percent1, String percent2) {
      int percent1Int = 0;
      boolean percent1Exception = false;
      try {
         percent1Int = new Integer(percent1).intValue();
      } catch (NumberFormatException ex) {
         percent1Exception = true;
      }
      int percent2Int = 0;
      boolean percent2Exception = false;
      try {
         percent2Int = new Integer(percent2).intValue();
      } catch (NumberFormatException ex) {
         percent2Exception = true;
      }
      int toReturn = 0;
      if (percent1Exception && percent2Exception)
         toReturn = getComparator().compare(percent1, percent2);
      else if (percent1Exception && !percent2Exception)
         toReturn = -1;
      else if (!percent1Exception && percent2Exception)
         toReturn = 1;
      else
         toReturn = getCompareForPercent(percent1Int, percent2Int);

      return toReturn;
   }

   @SuppressWarnings("unchecked")
   public int getCompareForPercent(int percent1, int percent2) {
      int compareInt = 0;
      if (percent1 == percent2) {
         compareInt = 0;
      } else if (percent1 == 0) {
         compareInt = -1;
      } else if (percent2 == 0) {
         compareInt = 1;
      } else if (percent1 == 100) {
         compareInt = 1;
      } else if (percent2 == 100) {
         compareInt = -1;
      } else {
         compareInt = getCompareForInteger(percent1, percent2);
      }
      return compareInt;
   }

}