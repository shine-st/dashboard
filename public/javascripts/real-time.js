/**
 * Created by shinest on 12/07/2017.
 */

function todaySummarize() {

    caller(jsRoutes.shine.st.dashboard.controllers.DataController.todaySummarize, '', function(json) {
        var web = json.WEB
        var app = json.APP

        $("#web-visitor-summarize").text(web.visitor)
        $("#app-visitor-summarize").text(app.visitor)

        var webConversionRate = (Math.round(web.conversionRate * 10000) / 100) + '%'
        var appConversionRate = (Math.round(app.conversionRate * 10000) / 100) + '%'
        $("#web-conversionRate-summarize").text(webConversionRate)
        $("#app-conversionRate-summarize").text(appConversionRate)
    })
}