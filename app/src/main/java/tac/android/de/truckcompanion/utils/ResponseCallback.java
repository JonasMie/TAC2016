package tac.android.de.truckcompanion.utils;

import com.android.volley.VolleyError;
import org.json.JSONObject;

/**
 * Created by Jonas Miederer.
 * Date: 08.05.16
 * Time: 16:00
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public interface ResponseCallback {
    /**
     * On success.
     *
     * @param result the result
     */
    void onSuccess(JSONObject result);

    /**
     * On error.
     *
     * @param error the error
     */
    void onError(VolleyError error);
}
