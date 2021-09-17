package si.vicos.annotations;

import java.awt.*;
import java.awt.geom.Point2D;
import java.text.ParseException;
import java.util.*;
import java.util.List;

import org.coffeeshop.string.StringUtils;

/**
 * The Class SegmentationMaskAnnotation.
 */
public class SegmentationMaskAnnotation extends ShapeAnnotation {

    /** The vector of true points */
    private Vector<Point> points;

    /** The mask */
    private boolean[][] mask;

    /** The offset*/
    private int x0;
    private int y0;
    private int width;
    private int height;

    /** */
    private boolean maskUpdated;

    /**
     * Instantiates a new segmenation mask annotation.
     */
    public SegmentationMaskAnnotation() {
        x0 = 0;
        y0 = 0;
        width = 0;
        height = 0;
        points = new Vector<Point>();
        mask = new boolean[0][0];
        maskUpdated = true;
    }

    public SegmentationMaskAnnotation(boolean[][] mask) {
        points = new Vector<Point>();
        if(mask != null)
        {
            for (int i = 0; i < mask.length; i++) {
                for (int j = 0; j < mask[i].length; j++) {
                    if(mask[i][j]) {
                        points.add(new Point(i,j));
                        if(j < x0) {
                            width += (x0-j) + 1;
                            x0 = j;
                        }
                        else if(j > (x0 + width))
                            width = j - x0  + 1;
                        if(i < y0) {
                            height += (y0-i) + 1;
                            y0 = i;
                        }
                        else if(i > (y0 + height))
                            height = i - y0 + 1;
                    }
                }
            }
        }
        this.mask = mask;
        maskUpdated = true;
    }

    public SegmentationMaskAnnotation(Vector<Point> points) {
        width = 0;
        height = 0;
        this.points = points;
        x0 = (int) points.get(0).getX();
        y0 = (int) points.get(0).getY();
        for (Point2D point : points) {
            int pointX0 = (int) point.getX();
            int pointY0 = (int) point.getY();
            if(pointX0 < x0) {
                width += (x0-pointX0) + 1;
                x0 = pointX0;
            }
            else if(pointX0 > (x0 + width))
                width = pointX0 - x0 + 1;
            if(pointY0 < y0) {
                height += (y0-pointY0) + 1;
                y0 = pointY0;
            }
            else if(pointY0 > (y0 + height))
                height = pointY0 - y0  + 1;
        }
        maskUpdated = false;
    }

    /**
     * Constructor for a mask from rle
     * */
    public SegmentationMaskAnnotation(String[] tokens) {
        this.x0 = Integer.parseInt(tokens[0].substring(1));
        this.y0 = Integer.parseInt(tokens[1]);
        this.width = Integer.parseInt(tokens[2]);
        this.height = Integer.parseInt(tokens[3]);
        points = rle_to_vector(tokens,width,height,x0,y0);
        maskUpdated = false;

    }

    private Vector<Point> rle_to_vector(String[] rle, int width, int height, int x0, int y0) {
        /**
         *     rle: input rle mask encoding
         *     each evenly-indexed element represents number of consecutive 0s
         *     each oddly indexed element represents number of consecutive 1s
         *     width and height are dimensions of the mask
         *     output: Vector of 2d points
         *     */

        points = new Vector<Point>();

        int idx_ = 0;
        for (int i = 4; i < rle.length; i++) {
            if(i % 2 != 0)
                //write as many trues as RLE says (falses are already in the vector)
                for (int j = 0; j < Integer.parseInt(rle[i]); j++) {
                    points.add(new Point(this.x0 + ((idx_ + j) % width),this.y0 + ((idx_ + j) / width)));
                }
            idx_ += Integer.parseInt(rle[i]);
        }

        return points;
    }
/*
    private static boolean[][] rle_to_mask(String[] rle, int width, int height) {
        /*
         *     rle: input rle mask encoding
         *     each evenly-indexed element represents number of consecutive 0s
         *     each oddly indexed element represents number of consecutive 1s
         *     width and height are dimensions of the mask
         *     output: 2-D binary mask
         *

        boolean[][] mask = new boolean[height][width];

        // set id of the last different element to the beginning of the vector
        int idx_ = 0;
        for (int i = 4; i < rle.length; i++) {
            if(i % 2 != 0)
                //write as many trues as RLE says (falses are already in the vector)
                for (int j = 0; j < Integer.parseInt(rle[i]); j++) {
                    mask[(idx_ + j) / width][(idx_ + j) % width] = true;
                }
            idx_ += Integer.parseInt(rle[i]);
        }
        return mask;
    }*/

    /** Getter for the mask */
    public boolean[][] getMask() {
        if(!this.maskUpdated) {
            this.mask = pointsToMask();
            this.maskUpdated = true;
        }
        return this.mask;
    }

    /** Adds new points to the vector of points */
    public void addPoints(Vector<Point> points) {
        if (this.points == null)
            points = new Vector<Point>();
        for (Point newPoint : points) {
             boolean remove = false;

            for (Point point : this.points) {
                if(point.x == newPoint.x && point.y == newPoint.y) {
                    remove = true;
                    break;
                }
            }

            if(remove)
                this.points.remove(newPoint);
            else {
                this.points.add(newPoint);
                int pointX0 = (int) newPoint.getX();
                int pointY0 = (int) newPoint.getY();

                if(this.points.size() == 1) {
                    this.x0 = pointX0;
                    this.y0 = pointY0;
                    this.width = 1;
                    this.height = 1;
                }

                if(pointX0 < x0) {
                    width += (x0-pointX0) + 1;
                    x0 = pointX0;
                }
                else if(pointX0 > (x0 + width))
                    width = pointX0 - x0  + 1;
                if(pointY0 < y0) {
                    height += (y0-pointY0) + 1;
                    y0 = pointY0;
                }
                else if(pointY0 > (y0 + height))
                    height = pointY0 - y0 + 1;
            }
            this.maskUpdated = false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see si.vicos.annotations.Annotation#pack()
     */
    @Override
    public String pack() {
        if(!this.maskUpdated)
            this.mask = pointsToMask();
        return isNull() ? "" : String.format(SERIALIZATION_LOCALE,
                "%s", rle_to_string(mask_to_rle()));
    }

    private String rle_to_string(int[] mask_to_rle) {
        StringBuilder sb = new StringBuilder();
        sb.append("m");
        sb.append(String.format("%d,%d,%d,%d,", x0, y0, width, height));
        for (int i = 0; i < mask_to_rle.length; i++) {
            sb.append(String.format("%d,",mask_to_rle[i]));
        }
        return sb.toString();
    }

    private boolean[][] pointsToMask() {
        boolean[][] mask = new boolean[height][width];
        for (Point point : points){
            mask[point.y-this.y0][point.x-this.x0] = true;
        }
        return mask;
    }

    /*
     * (non-Javadoc)
     *
     * The mask to rle function
     */
    private int[] mask_to_rle(){
        /**
         * Input: 2-D array
         * Output: array of numbers (1st number = #0s, 2nd number = #1s, 3rd number = #0s, ...)
         */
        // reshape mask to a list

        if(!this.maskUpdated)
            this.mask = pointsToMask();

        List<Boolean> l = new ArrayList<>();
        for (boolean[] row : this.mask) {
            for (boolean pixel : row) {
                l.add(pixel);
            }
        }
        // output is empty at the begining
        List<Integer> rle = new ArrayList<>();

        // index of the last different element
        int last_idx = 0;

        // check if first element is 1, so first element in RLE (number of zeros) must be set to 0
        if(l.get(0))
            rle.add(0);

        // go over all elements and check if two consecutive are the same
        for (int i = 1; i < l.size(); i++) {
            if( l.get(i) != l.get(i - 1) )
            {
                rle.add(i - last_idx);
                last_idx = i;
            }
        }

        if(l.size() > 0){
            // handle last element of rle
            if( last_idx < l.size() - 1 )
                // last element is the same as one element before it - add number of these last elements
                rle.add(l.size() - last_idx);
            else
                // last element is different than one element before - add 1
                rle.add(1);
        }

        int[] rleArray = new int[rle.size()];
        for (int i = 0; i < rle.size(); i++) {
            rleArray[i] = rle.get(i);
        }
        return rleArray;
    }

    /*
     * (non-Javadoc)
     *
     * @see si.vicos.annotations.Annotation#reset()
     */
    @Override
    public void reset() {
        //reinitialize mask annotation
        x0 = 0;
        y0 = 0;
        width = 0;
        height = 0;
        points = new Vector<Point>();
        mask = new boolean[0][0];
        maskUpdated = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see si.vicos.annotations.Annotation#unpack(java.lang.String)
     */
    @Override
    public void unpack(String data) throws ParseException {
        if (StringUtils.empty(data))
            return;

        try {
           /* String lines[] = data.split("\\r?\\n");
            int width = lines.length;
            int height = lines[0].split(",").length;
            Boolean [][] pack = new Boolean[width][height];

            for (int i = 0; i < width; i++) {
                lines[i] = lines[i].replaceAll("\\[", "");
                lines[i] = lines[i].replaceAll("\\]", "");
                String[] line = lines[i].split(",");
                for (int j = 0; j < line.length; j++) {
                    if(line[j].equals("true"))
                        mask[i][j] = true;
                    else
                        mask[i][j] = false;
                }

            }*/


        } catch (NoSuchElementException e) {
            throw new ParseException("Unable to parse", -1);
        } catch (NumberFormatException e) {
            throw new ParseException("Unable to parse", -1);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see si.vicos.annotations.Annotation#clone()
     */
    @Override
    public Annotation clone() {
        return new SegmentationMaskAnnotation(points);
    }

    /**
     * Changes the pixels value
     */
    private void set(int x, int y) {
        Point changedPoint = new Point(x,y);
        boolean remove = false;

        for (Point point : points) {
            if(point.x == changedPoint.x && point.y == changedPoint.y) {
                remove = true;
                break;
            }
        }

        if(remove)
            points.remove(changedPoint);
        else
            points.add(changedPoint);
        this.maskUpdated = false;
    }


    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return rle_to_string(mask_to_rle());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * si.vicos.annotations.Annotation#validate(si.vicos.annotations.Annotation)
     */
    @Override
    public boolean validate(Annotation a) {
        return a instanceof SegmentationMaskAnnotation;
    }

    /*
     * (non-Javadoc)
     *
     * @see si.vicos.annotations.Annotation#getType()
     */
    @Override
    public AnnotationType getType() {
        return AnnotationType.SEGMENTATION_MASK;
    }

    /*
     * (non-Javadoc)
     *
     * @see si.vicos.annotations.Annotation#canInterpolate()
     */
    @Override
    public boolean canInterpolate() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see si.vicos.annotations.Annotation#scale(float)
     */
    @Override
    public Annotation scale(float scale) throws UnsupportedOperationException {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see si.vicos.annotations.ShapeAnnotation#getBoundingBox()
     */
    @Override
    public RectangleAnnotation getBoundingBox() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see si.vicos.annotations.Annotation#isNull()
     */
    @Override
    public boolean isNull() {
        return points == null || points.size() == 0;
    }

    /**
     * Contains.
     *
     * @param a
     *            the a
     * @return true, if successful
     */
    public boolean contains(PointAnnotation a) {

        return points.contains(new Point((int)a.getX(),(int)a.getY()));

    }


    @Override
    public Point2D getCenter() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see si.vicos.annotations.ShapeAnnotation#getPolygon()
     */
    @Override
    public List<Point2D> getPolygon() {
      return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * si.vicos.annotations.Annotation#convert(si.vicos.annotations.Annotation)
     */
    @Override
    public Annotation convert(Annotation a)
            throws UnsupportedOperationException {

        if (a instanceof RectangleAnnotation)
            return a;

        if (a instanceof ShapeAnnotation) {
            return ((ShapeAnnotation) a).getBoundingBox();
        }

        return super.convert(a);
    }

    public int getY0() {
        return y0;
    }

    public int getX0() {
        return x0;
    }
}