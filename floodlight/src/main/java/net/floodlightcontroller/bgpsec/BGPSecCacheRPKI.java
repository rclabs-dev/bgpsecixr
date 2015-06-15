package net.floodlightcontroller.bgpsec;

import net.floodlightcontroller.learningswitch.LearningSwitch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BGPSecCacheRPKI {
	protected static Logger log = LoggerFactory.getLogger(BGPSecCacheRPKI.class); 
    private final static int BASE_TIME = 60; // TTL Cache: value in minutes. This should be synchronized with the RPKI 
    private final static long TTL = 1000 * 60 * BASE_TIME; 
	
    /** 
     * Query to the cache, initialize and reinitialize when out of date
     * @param prefix
     * @param asn
     * @param ip
     * @return
     */
	public static boolean getROAOnCache(String prefix, String asn, String ip) {
		long nowTime = System.currentTimeMillis();
		if (LearningSwitch.cacheMap.size() == 0) { // Cache initialize 
			LearningSwitch.cacheMap.put("TTL", new String[] {String.valueOf(nowTime), ""});
			log.info("SecBGP: Cache TTL was initialized:" + LearningSwitch.cacheMap.size());
			return false;
	    } else { // Verify whether the cache is out of date. If yes, reinitialize.
	    	String[] getTTL = LearningSwitch.cacheMap.get("TTL");
	    	long timeStamp = nowTime - Long.valueOf(getTTL[0].toString().replaceAll("[\\[\\]]",""));
	    	if (timeStamp > TTL) {
	    		LearningSwitch.cacheMap.clear();
	    		LearningSwitch.cacheMap.put("TTL", new String[] {String.valueOf(nowTime), ""});
	    		log.info("SecBGP: Cache TTL was reinitialized.");
	    		return false;
	    	}
	    }			
		// Querying whether ASN/Prefix exist in cache 
		if (LearningSwitch.cacheMap.containsKey(prefix)){
			String[] values = LearningSwitch.cacheMap.get(prefix);
			// Verify NLRI routes
			if (ip.equals("") && asn.equals(values[0]))
				return true;    
			// Verify withdraw routes
			if (asn.equals("") && ip.equals(values[1]))
				return true;   			
		}
	    return false;
	}
	
	/**
	 *  Add new data in the cache
	 * @param prefix
	 * @param asn
	 * @param ip
	 * @return
	 */
	public static boolean setROAOnCache(String prefix, String asn, String ip) {
		LearningSwitch.cacheMap.put(prefix, new String[] {asn, ip});
		log.info("SecBGP: Add ROA " + asn + "/" + prefix + ", from Speaker " + 
		                    ip + ", Cache size: " + LearningSwitch.cacheMap.size());
		return true;
	}
}
