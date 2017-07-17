/**
 * Created by shinest on 12/07/2017.
 */


function caller(api, queryString, callback) {
    var apiAjax = api();

    $.ajax({
        url: queryString ? `${apiAjax.url}?${queryString}` : apiAjax.url,
        type: apiAjax.type,
        success: function(json) {
            callback(json)
        }
    });
}