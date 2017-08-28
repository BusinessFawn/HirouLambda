package xyz.tay.postorder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.kinesisanalytics.model.ResourceNotFoundException;

public class PostOrder implements RequestHandler<Object, String> {
	
	String tableName = "Orders";
	StringBuilder stringBuilder = new StringBuilder();

    @Override
    public String handleRequest(Object input, Context context) {
        context.getLogger().log("Input: " + input);
        
        
        HashMap<String, AttributeValue> itemValues = getObjectsFromJSON(input.toString(),context);
        final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
        HashMap<String, String> responseMap = new HashMap<String, String>();
        try {
        	ddb.putItem(tableName, itemValues);
        	responseMap.put("response", "success");
        } catch(ResourceNotFoundException e) {
        	context.getLogger().log(e.getErrorMessage());
        	responseMap.put("response", e.getErrorMessage());
        } catch(AmazonServiceException e) {
        	context.getLogger().log(e.getErrorMessage());
        	responseMap.put("response", e.getErrorMessage());
        }

        return new JSONObject(responseMap).toString();
    }
    
    public HashMap<String, AttributeValue> getObjectsFromJSON(String input, Context context) {
    	
    	HashMap<String, AttributeValue> itemValues = new HashMap<String, AttributeValue>();
    	try {
    		JSONObject fullObject = new JSONObject(input);
	    	for(int i = 0; i < fullObject.names().length(); i++) {
	    		
	    		String key = fullObject.names().getString(i);
	    		Object obVal = fullObject.opt(fullObject.names().getString(i));
	    		AttributeValue value = new AttributeValue();
	    		if(obVal.getClass().isInstance(i)) {
	    			value.setN(obVal.toString());
	    			context.getLogger().log("Adding a this key, value pair: " + key + ":" + value);
	    		}
	    		else if(obVal.getClass().isInstance(key)) {
	    			value.setS(obVal.toString());
	    			context.getLogger().log("Adding a this key, value pair: " + key + ":" + value);
	    		}
	    		else {
	    			String attemptedList = obVal.toString();
	    			if(attemptedList.contains("[{")) {
	    				JSONArray obList = new JSONArray(attemptedList);
	    				Collection<AttributeValue> attCollection = new ArrayList<AttributeValue>();
	    				for(int j = 0; j < obList.length(); j++) {
	    					AttributeValue singleAtt = new AttributeValue();
	    					singleAtt.setM(getObjectsFromJSON(obList.optJSONObject(j).toString(),context));
	    					attCollection.add(singleAtt);
	    				}
	    				value.setL(attCollection);
	    				
	    				context.getLogger().log("Working with a list! " + obVal.toString());
	    			}
	    			else if(attemptedList.contains("{")) {
	    				value.setM(getObjectsFromJSON(attemptedList,context));
	    				context.getLogger().log("Working with a single JSONObject! " + obVal.toString());
	    			}
	    			else {
	    				value.setS("non");
	    				context.getLogger().log("Not sure what to do here... " + obVal.toString());
	    			}
	    		}
	    		itemValues.put(key, value);
	    	}
	    	return itemValues;
    	}
    	catch(JSONException e) {
    		context.getLogger().log(e.getMessage());
    		AttributeValue errorMessage = new AttributeValue(e.getMessage());
    		HashMap<String, AttributeValue> errorMap = new HashMap<String,AttributeValue>();
    		
    		errorMap.put("error", errorMessage);
    		return errorMap;
    	}
    }

}