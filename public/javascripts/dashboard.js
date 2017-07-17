var currentCategoryId


function init() {
    currentCategoryId = '1'
    var beginDate = moment().subtract(7, 'days');
    var endDate = moment().subtract(1, 'days');
    updateWholeChart(beginDate, endDate, currentCategoryId, '7d')
}



function updateDailyDataChart(beginDate, endDate, categoryId, device, timeTag, drawChart) {
    var axisXFormat = getTimeFormatFromTimeTag(timeTag)
    var timeGroup = getTimeGroupFromTimeTag(timeTag)
    var api = timeTag == 'today' ? jsRoutes.shine.st.dashboard.controllers.DataController.todayDeviceHourly : jsRoutes.shine.st.dashboard.controllers.DataController.device

    const begin = formatDate(beginDate),
        end = formatDate(endDate)

    const queryString = `begin=${begin}&end=${end}&categoryId=${categoryId}&device=${device}&timeGroup=${timeGroup}`
    caller(api, queryString, function(json) {
        var data;
        switch(timeTag) {
            case 'today':
                var hourly = dailyDataClean(json, 'hourly')
                data = [hourly]
                break;

            case '7d':
            case '1m':
                var daily = dailyDataClean(json, 'daily')
                var average = dailyDataClean(json, 'average')

                data = [daily, average]
                break;

            case '12m':
                var month = dailyDataClean(json, 'month')
                data = [month]
                break;
        }

        drawChart(data, axisXFormat)
    })
}


function updateOrderSourceChart(beginDate, endDate, categoryId, timeTag) {
    var axisXFormat = getTimeFormatFromTimeTag(timeTag)
    var timeGroup = getTimeGroupFromTimeTag(timeTag)
    var api = timeTag == 'today' ? jsRoutes.shine.st.dashboard.controllers.DataController.todayOrderSourceHourly : jsRoutes.shine.st.dashboard.controllers.DataController.orderSource

    const begin = formatDate(beginDate),
        end = formatDate(endDate)

    const queryString = `begin=${begin}&end=${end}&categoryId=${categoryId}&timeGroup=${timeGroup}`

    caller(api, queryString, function(json) {
        var data = [sourceFilter('DIRECT',json), sourceFilter('EDM',json), sourceFilter('GOOGLE_AD',json), sourceFilter('FACEBOOK_AD',json)]

        orderSourceChart(data, axisXFormat)
    })
}

function updateOrderCustomerChart(beginDate, endDate, categoryId, timeTag) {
    var axisXFormat = getTimeFormatFromTimeTag(timeTag)
    var timeGroup = getTimeGroupFromTimeTag(timeTag)

    const begin = formatDate(beginDate),
        end = formatDate(endDate)

    const queryString = `begin=${begin}&end=${end}&categoryId=${categoryId}&timeGroup=${timeGroup}`

    caller(jsRoutes.shine.st.dashboard.controllers.DataController.orderCustomer, queryString, function(json) {
        var parseTime = d3.timeParse(D3DateTimeFormat);

        var data = json.map((d) => {
                d['date'] = parseTime(d.date);
        return d;
        })

        orderCustomerChart(data, axisXFormat)
    })
}



function updateWholeChart(beginDate, endDate, categoryId, timeTag) {
    updateDailyDataChart(beginDate, endDate, categoryId, 'WEB', timeTag, function(data, axisXFormat) {
        multiLineChart(data, 'visitor', 'web-visitor', axisXFormat)
        multiLineChart(data, 'conversionRate', 'web-conversionRate', axisXFormat)
    })


    updateDailyDataChart(beginDate, endDate, categoryId, 'APP', timeTag, function(data, axisXFormat) {
        multiLineChart(data, 'visitor', 'app-visitor', axisXFormat)
        multiLineChart(data, 'conversionRate', 'app-conversionRate', axisXFormat)
    })

    updateOrderSourceChart(beginDate, endDate, categoryId, timeTag)
    updateOrderCustomerChart(beginDate, endDate, categoryId, timeTag)

}

function dailyDataClean(data, id) {
    var parseTime = d3.timeParse(D3DateTimeFormat);

    return {
        id: id,
        data: data[id].map((d) => {
            d['date'] = parseTime(d.date)
            d['conversionRate'] = d.order_count / d.page_view
            return d
        })
    }
}

function sourceFilter(key, source) {
    var parseTime = d3.timeParse(D3DateTimeFormat);
    return {
        id: key,
        data: source.filter((d) => d.source == key)
                .map((d) => {
                    d['date'] = parseTime(d.date);
                    return d;
                })
    }
}


function onCategoryClick(categoryId) {
    return function(event) {
        $(".btn-group > button.btn").removeClass('btn-success');
        $(".btn-group > button.btn").each(function(index) {
            if(this.id.indexOf("7d") > -1)
                $(this).addClass('btn-success')
        })

        var sevenDaysAgo = moment().subtract(7, 'days');
        var yesterday = moment().subtract(1, 'days');
        currentCategoryId = categoryId
        updateWholeChart(sevenDaysAgo, yesterday, categoryId, '7d')
    }
}



function onDailyChartTimeClick(event) {
    $(this).parent().children().removeClass('btn-success');
    $(this).addClass('btn-success')

        var info = this.id.split("-"),
            device = info[1],
            yDimension = info[2],
            id = device + '-' + yDimension,
            timeTag = info[3],
            beginDate = getDateFromTimeTag(timeTag),
            endDate = moment().subtract(1, 'days');

        updateDailyDataChart(beginDate, endDate, currentCategoryId, device.toUpperCase(), timeTag, function(data, axisXFormat) {
            multiLineChart(data, yDimension, id, axisXFormat)
        })

}

function onOrderChartTimeClick(event) {
    $(this).parent().children().removeClass('btn-success');
    $(this).addClass('btn-success')

    var info = this.id.split("-"),
        chartType = info[2],
        timeTag = info[3],
        beginDate = getDateFromTimeTag(timeTag),
        endDate = moment().subtract(1, 'days');

    if(chartType == 'source')
        updateOrderSourceChart(beginDate, endDate,currentCategoryId, timeTag)
    else if(chartType == 'customer')
        updateOrderCustomerChart(beginDate, endDate,currentCategoryId, timeTag)


}



