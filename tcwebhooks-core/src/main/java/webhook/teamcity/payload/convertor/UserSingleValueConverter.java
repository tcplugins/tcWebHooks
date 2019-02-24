package webhook.teamcity.payload.convertor;

import com.thoughtworks.xstream.converters.SingleValueConverter;

import jetbrains.buildServer.users.User;

public class UserSingleValueConverter implements SingleValueConverter {
 
    @Override
    public boolean canConvert(Class clazz) {
        return User.class.isAssignableFrom(clazz);
    }
 
    @Override
    public String toString(Object obj) {
        String username = ((User) obj).getUsername();
        return username;
    }

    /* 
     * We don't support unmarshalling. One way only I'm afraid.
     */
    
	@Override
	public Object fromString(String str) {
		return null;
	}
	
}