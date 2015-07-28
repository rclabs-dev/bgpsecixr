package net.floodlightcontroller.bgpsec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyOfBGPSecQueryRPKI {
	protected static Logger log = LoggerFactory
			.getLogger(CopyOfBGPSecQueryRPKI.class);
	static ArrayList<String> prefixData = new ArrayList<String>();

	public static boolean roaValidator(String prefixes, String asn,
			String speaker, int type) {
		prefixData = BGPSecPrefixChainParser.prefixParser(prefixes);
		// Iterator for the total of NLRI/Withdraw prefixes
		for (String eachPrefix : prefixData) {
			if (type == 1) {
				// Check prefix in NLRI at RPKI Cache
				String asnPrefix = asn + "/" + eachPrefix;
				log.info("ASN/Prefix to verify on RPKI: " + asnPrefix);

				if (!(BGPSecCacheRPKI.getROAOnCache(eachPrefix, asn, ""))) {
					// Check prefix in RPKI Anchors database
					if (!(getROAStatus(asnPrefix))) {
						// Return false even whether at least of them is invalid
						log.info("ASN/Prefix NOT EXIST in DATABASE: "
								+ asnPrefix);
						return false;
					} else {
						// Prefix found in RPKI database, add in RPKI Cache
						log.info("ASN/Prefix EXIST in DATABASE: " + asnPrefix);
						BGPSecCacheRPKI.setROAOnCache(eachPrefix, asn, speaker);
					}
				}
			} else {
				// Check withdraw prefixes on RPKI Cache
				log.info("Withdraw routes, prefix to verify on RPKI: " + eachPrefix + ", Speaker:  " + speaker);
				if (!(BGPSecCacheRPKI.getROAOnCache(eachPrefix, "", speaker)))
					return false;
			}
		}
		return true;
	}

	/**
	 * HTTP request to RPKI and parse JSON result
	 * 
	 * @param url
	 * @return
	 */
	public static boolean getROAStatus(String data) {
		JSONObject mainObject;
		String resultRPKI = null;
		InputStream is = null;
		try {
			is = new URL((BGPSecDefs.RPKI_URL + data)).openStream();
			StringBuilder sb = new StringBuilder();
			int cp;
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			while ((cp = rd.read()) != -1) {
				sb.append((char) cp);
			}
			JSONObject json = null;
			json = new JSONObject(sb.toString());
			mainObject = (JSONObject) json.get("validated_route");
			JSONObject validityObj = (JSONObject) mainObject.get("validity");
			resultRPKI = validityObj.getString("state");

		} catch (MalformedURLException e) {
			log.info("MalformedURL Error em JSON Parser!");
			// e.printStackTrace();
		} catch (IOException e) {
			log.info("I/O Error em JSON Parser!");
			// e.printStackTrace();
		} catch (JSONException e) {
			log.info("JSON Error em JSON Parser!");
			// e.printStackTrace();
		}

		if (resultRPKI.equals("Valid"))
			return true;
		return false;
	}

}