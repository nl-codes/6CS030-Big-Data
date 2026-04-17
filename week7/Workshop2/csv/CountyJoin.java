/* 
   **************************************************
   Java file to join the pay and population csv files
   MG March 2019 
  ***************************************************
*/

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 
 public class CountyJoin {
	public static class PayMapper extends Mapper <Object, Text, Text, Text> {
	  public void map(Object key, Text value, Context context)
		throws IOException, InterruptedException {
		String record = value.toString();
		String[] parts = record.split(",");
		// 0: Key (county) 1: Year 2: figure
		// default to 0 if null value
		if (parts.length == 3 )
			context.write(new Text(parts[0]), new Text("pay\t" + parts[2]));
		else
			context.write(new Text(parts[0]), new Text("pay\t" + 0));
		} // map method
	} // PayMapper
 
	public static class PopMapper extends Mapper <Object, Text, Text, Text> {
	  public void map(Object key, Text value, Context context) 
		throws IOException, InterruptedException {
		String record = value.toString();
		String[] parts = record.split(",");
		// 0: Key (county) 1: Year 2: figure
		// default to 0 if null value
		if (parts.length == 3 )
			context.write(new Text(parts[0]), new Text("pop\t" + parts[2]));
		else
			context.write(new Text(parts[0]), new Text("pop\t" + 0));
		} // map method
	} // PopMapper
 
	public static class JoinReducer extends Reducer <Text, Text, Text, Text> {
	  public void reduce(Text key, Iterable<Text> values, Context context)
		throws IOException, InterruptedException {
		String payName = "";
		String popName = "";
		double payTotal = 0.0;
		int popTotal = 0;
		int popCount = 0;
		int payCount = 0;
 
		for (Text t : values) { 
			String parts[] = t.toString().split("\t");
			if (parts[0].equals("pop")) {
				popCount++;
				popTotal += Integer.parseInt(parts[1]);
				} // if 
			else if (parts[0].equals("pay")) {
				payCount++;
				payTotal += Float.parseFloat(parts[1]);
				} // else
			} // for loop

		String str = String.format("%d\t%f\t%d\t%d", payCount, payTotal, popCount, popTotal);
		context.write(new Text(key), new Text(str));
		} // reduce method
	} // JoinReducer
 
 public static void main(String[] args) throws Exception {
 
	Configuration conf = new Configuration();
	Job job = Job.getInstance(conf, "Join datasets");
	job.setJarByClass(CountyJoin.class);
	job.setReducerClass(JoinReducer.class);
	job.setOutputKeyClass(Text.class);
	job.setOutputValueClass(Text.class);
  
	MultipleInputs.addInputPath(job, new Path(args[0]),TextInputFormat.class, PayMapper.class);
	MultipleInputs.addInputPath(job, new Path(args[1]),TextInputFormat.class, PopMapper.class);
	
	Path outputPath = new Path(args[2]);
  	FileOutputFormat.setOutputPath(job, outputPath);
	// Delete the output directory
	outputPath.getFileSystem(conf).delete(outputPath, true);
	
	System.exit(job.waitForCompletion(true) ? 0 : 1);
 }
}


