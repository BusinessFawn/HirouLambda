package xyz.tay.postorder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.kinesisanalytics.model.ResourceNotFoundException;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;

public class PostOrder implements RequestHandler<Object, String> {
	
	String tableName = "Orders";
	StringBuilder stringBuilder = new StringBuilder();

    @Override
    public String handleRequest(Object input, Context context) {
        context.getLogger().log("Input: " + input);
        
        
        HashMap<String, AttributeValue> itemValues = new HashMap<String, AttributeValue>();
        final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
        
        
        try {
        	JSONObject fullObject = new JSONObject(input.toString());
        	
        	for(int i = 0; i < fullObject.names().length(); i++) {
        		
        		String key = fullObject.names().getString(i);
        		Object obVal = fullObject.opt(fullObject.names().getString(i));
        		AttributeValue value = new AttributeValue();
        		ArrayList arrayList = new ArrayList();
        		String[] stringList = {"123"};
        		int[] intList = {1,2,3};
        		JSONObject[] jsonList = {fullObject};
        		boolean boolVal = true;
        		if(obVal.getClass().isInstance(i)) {
        			value.setN(obVal.toString());
        		}
        		else if(obVal.getClass().isInstance(key)) {
        			value.setS(obVal.toString());
        		}
        		else if(obVal.getClass().isInstance(arrayList)){
        			value.setS("ArrayList!");
        		}
        		else if(obVal.getClass().isInstance(stringList)){
        			value.setS("Stringss!");
        		}
        		else if(obVal.getClass().isInstance(intList)){
        			value.setS("Intsssss!");
        		}
        		else if(obVal.getClass().isInstance(jsonList)){
        			value.setS("JSON!");
        		}
        		else if(obVal.getClass().isInstance(boolVal)) {
        			
        			boolVal = (boolean) obVal;
        			value.setBOOL(boolVal);
        		}
        		else {
        			value.setS("Something else....");
        			context.getLogger().log(obVal.toString());
        		}
        		
        		itemValues.put(key, value);
        		
        	}
        } catch(JSONException ex) {
        	context.getLogger().log(ex.toString());
        }
        
        
        try {
        	ddb.putItem(tableName, itemValues);
        } catch(ResourceNotFoundException e) {
        	context.getLogger().log(e.getErrorMessage());
        } catch(AmazonServiceException e) {
        	context.getLogger().log(e.getErrorMessage());
        }

        // TODO: implement your handler
        context.getLogger().log(itemValues.toString());
        return itemValues.toString();
    }

}