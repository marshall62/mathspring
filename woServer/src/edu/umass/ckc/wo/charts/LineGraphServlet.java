package edu.umass.ckc.wo.charts;

import org.jCharts.axisChart.AxisChart;
import org.jCharts.axisChart.customRenderers.axisValue.renderers.ValueLabelPosition;
import org.jCharts.axisChart.customRenderers.axisValue.renderers.ValueLabelRenderer;
import org.jCharts.chartData.AxisChartDataSet;
import org.jCharts.chartData.ChartDataException;
import org.jCharts.chartData.interfaces.IAxisDataSeries;
import org.jCharts.encoders.ServletEncoderHelper;
import org.jCharts.properties.*;
import org.jCharts.properties.util.ChartFont;
import org.jCharts.types.ChartType;
import org.jCharts.test.TestDataGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.awt.*;
import java.io.IOException;


public class LineGraphServlet extends HttpServlet
{
	//---all of my charts serviced by this Servlet will have the same properties.
	private LineChartProperties lineChartProperties;

	//---all of my charts serviced by this Servlet will have the same properties.
	protected LegendProperties legendProperties;
	protected AxisProperties axisProperties;
	protected ChartProperties chartProperties;

	protected int width = 1620;
	protected int height = 500;


	/**********************************************************************************************
	 *
	 **********************************************************************************************/
	public void init()
	{
		this.legendProperties = new LegendProperties();
		this.chartProperties = new ChartProperties();
		this.axisProperties = new AxisProperties( false );
		ChartFont axisScaleFont = new ChartFont( new Font( "Georgia Negreta cursiva", Font.PLAIN, 13 ), Color.black );
		axisProperties.getXAxisProperties().setScaleChartFont( axisScaleFont );
		axisProperties.getYAxisProperties().setScaleChartFont( axisScaleFont );

		ChartFont axisTitleFont = new ChartFont( new Font( "Arial Narrow", Font.PLAIN, 14 ), Color.black );
		axisProperties.getXAxisProperties().setTitleChartFont( axisTitleFont );
		axisProperties.setXAxisLabelsAreVertical( true );
		axisProperties.getYAxisProperties().setTitleChartFont( axisTitleFont );

		DataAxisProperties dataAxisProperties = (DataAxisProperties) axisProperties.getYAxisProperties();

		try
		{
			dataAxisProperties.setUserDefinedScale( 0, 1 );
			dataAxisProperties.setNumItems(2);
		}
		catch( PropertyException propertyException )
		{
			propertyException.printStackTrace();
		}
//        dataAxisProperties.setUsePercentSigns(true);
		//dataAxisProperties.setRoundToNearest( 2 );

		ChartFont titleFont = new ChartFont( new Font( "Georgia Negreta cursiva", Font.PLAIN, 14 ), Color.black );
		this.chartProperties.setTitleFont( titleFont );
        Stroke[] strokes= { LineChartProperties.DEFAULT_LINE_STROKE,LineChartProperties.DEFAULT_LINE_STROKE };
        Shape[] shapes= { PointChartProperties.SHAPE_CIRCLE,PointChartProperties.SHAPE_SQUARE };
        lineChartProperties= new LineChartProperties( strokes, shapes );


//		ValueLabelRenderer valueLabelRenderer = new ValueLabelRenderer( false, false, true, -1 );
//		valueLabelRenderer.setValueLabelPosition( ValueLabelPosition.ON_TOP );
//		valueLabelRenderer.useVerticalLabels( false );


	}


	/**********************************************************************************************
	 *
	 **********************************************************************************************/
	public void service( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
	{
		try
		{
            HttpSession sess = req.getSession();
            IAxisDataSeries dataSeries = (IAxisDataSeries) sess.getAttribute("dataSeries");
            double[][] data = (double[][]) sess.getAttribute("data");
            String[] legendLabels = (String[]) sess.getAttribute("legendLabels");
            Stroke[] strokes = new Stroke[legendLabels.length];
            Shape[] shapes = new Shape[legendLabels.length];
            for (int i=0;i<legendLabels.length;i++) {
                strokes[i]=LineChartProperties.DEFAULT_LINE_STROKE;
                shapes[i]= PointChartProperties.SHAPE_CIRCLE;
            }
            lineChartProperties= new LineChartProperties( strokes, shapes );
            width = data[0].length * 38;

            Paint[] paints =  TestDataGenerator.getRandomPaints( legendLabels.length );

            dataSeries.addIAxisPlotDataSet( new AxisChartDataSet( data, legendLabels, paints, ChartType.LINE, this.lineChartProperties ) );

			AxisChart axisChart = new AxisChart( dataSeries, this.chartProperties, this.axisProperties, this.legendProperties, this.width, this.height );
			ServletEncoderHelper.encodeJPEG13( axisChart, 1.0f, resp );


        } catch (PropertyException e1) {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ChartDataException e1) {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}