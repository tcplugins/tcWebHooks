package webhook.teamcity.payload.util;

import static org.junit.Assert.*;

import org.junit.Test;

import webhook.teamcity.payload.util.TemplateMatcher.VariableResolver;

public class TemplateMatcherTest {

	/*-------------------------------------------------[ Testing ]---------------------------------------------------*/

	@Test
	public void test() {
		
		

	        String test1 = (new TemplateMatcher("${", "}").replace("this is ${santhosh}ghgjh\n ${kumar} sdf ${tekuri}abc", new VariableResolver(){
	            @Override
	            public String resolve(String variable){
	                if(variable.equals("santhosh"))
	                    return null;
	                return variable.toUpperCase();
	            }

				@Override
				public Object put(String key, Object value) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Object get(String key) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public boolean containsKey(Object key) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public Object[] getKeys() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Object remove(Object key) {
					// TODO Auto-generated method stub
					return null;
				}
	        }));
	        
	        assertTrue(test1.equals("this is ${santhosh}ghgjh\n KUMAR sdf TEKURIabc"));

	        String test2 = (new TemplateMatcher("$").replace("this is $santhosh ghgjh\n $kumar sdf $tekuri\n$ abc", new VariableResolver(){
	            @Override
	            public String resolve(String variable){
	                return variable.toUpperCase();
	            }

				@Override
				public Object put(String key, Object value) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Object get(String key) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public boolean containsKey(Object key) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public Object[] getKeys() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Object remove(Object key) {
					// TODO Auto-generated method stub
					return null;
				}
	        }));
	        
	        assertTrue(test2.equals("this is SANTHOSH ghgjh\n KUMAR sdf TEKURI\n abc"));
	}

}
