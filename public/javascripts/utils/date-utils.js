/**
 * Created by shinest on 09/07/2017.
 */

const DateTimeFormat = 'YYYY-MM-DD';

// const DateTimeHourFormat = 'YYYY-MM-DD HH:mm';

// const DateTimeHourSecFormat = 'YYYY-MM-DD HH:mm:ss';

const D3DateTimeFormat = '%Y-%m-%d %H:%M:%S'

function format(date, fromatString) {
    return moment(date).format(fromatString)
}

function formatDate(date) {
    return format(date, DateTimeFormat)
}

function getDateFromTimeTag(tag) {
    var beginDate
    switch(tag) {
        case '7d':
            beginDate = moment().subtract(7, 'days');
            break;
        case '1m':
            beginDate = moment().subtract(1, 'months');
            break;
        case '12m':
            beginDate = moment().subtract(1, 'years');
            break;
        default:

    }

    return beginDate;
}

function getTimeFormatFromTimeTag(tag) {
    var axisXFormat;

    switch(tag) {
        case 'today':
            axisXFormat =
                {
                    unit: '時',
                    format: '%H'
                }
            break;
        case '7d':
        case '1m':
            axisXFormat =
                {
                    unit: '天',
                    format: '%d'
                }
            break;
        case '12m':
            axisXFormat =
                {
                    unit: '月',
                    format: '%m'
                }
            break;
        default:
            axisXFormat =
                {
                    unit: '天',
                    format: '%d'
                }
    }

    return axisXFormat;
}

function getTimeGroupFromTimeTag(tag) {
    var timeGroup;

    switch(tag) {
        case 'today':
            timeGroup = 'HOURLY'
            break;
        case '7d':
        case '1m':
            timeGroup = 'DAILY'
            break;
        case '12m':
            timeGroup = 'MONTH'
            break;
        default:
            timeGroup = 'DAILY'
    }

    return timeGroup;
}