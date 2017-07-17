/**
 * Created by shinest on 09/07/2017.
 */

function multiLineChart(dataArr, yDimension, id, axisXFormat) {
    var svg = d3.select("#" + id)

    svg.selectAll("*").remove();

        var margin = {top: 20, right: 80, bottom: 30, left: 50},
        width = svg.attr("width") - margin.left - margin.right,
        height = svg.attr("height") - margin.top - margin.bottom,
        g = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");


    var x = d3.scaleTime().range([0, width]),
        y = d3.scaleLinear().range([height, 0]),
        z = d3.scaleOrdinal(d3.schemeCategory10);

    var line = d3.line()
        // .curve(d3.curveBasis)
        .curve(d3.curveCardinal.tension(0.8))
        .x(function (d) {
            return x(d.date);
        })
        .y(function (d) {
            return y(d[yDimension]);
        });



    x.domain([
        d3.min(dataArr, function (c) {
            return d3.min(c.data, function (d) {
                return d.date;
            });
        }),
        d3.max(dataArr, function (c) {
            return d3.max(c.data, function (d) {
                return d.date;
            });
        })
    ]);



    y.domain([
        d3.min(dataArr, function (c) {
            return d3.min(c.data, function (d) {
                return d[yDimension];
            });
        }),
        d3.max(dataArr, function (c) {
            return d3.max(c.data, function (d) {
                return d[yDimension];
            });
        })
    ]);

    z.domain(dataArr.map(function (c) {
        return c.id;
    }));


    var axisX = d3.axisBottom(x)
            .ticks(dataArr[0].data.length)
            .tickFormat((d) => {
                return d3.timeFormat(axisXFormat.format)(d);
            });

    var axisY = d3.axisLeft(y)
            .ticks(5)
            .tickFormat((d) => {
            if(yDimension == 'conversionRate')
    return (d * 1000 / 10) + '%'
else
    return d
})


    g.append("g")
        .attr("class", "axis axis--x")
        .attr("transform", "translate(0," + height + ")")
        .call(axisX)
        .append("text")
        .attr("x", x(dataArr[0].data[dataArr[0].data.length -1].date) + 20)
        .attr("y", 10)
        .attr("dy", "0.71em")
        .attr("fill", "#000")
        .text("(" + axisXFormat.unit + ")");

    g.append("g")
        .attr("class", "axis axis--y")
        .call(axisY)


    var dimension = g.selectAll("." + yDimension)
        .data(dataArr)
        .enter().append("g")
        .attr("class", yDimension);

    dimension.append("path")
    // .attr("class", "line")
        .attr("d", function (d) {
            return line(d.data);
        })
        .attr("fill", "none")
        .attr("stroke", function (d) {
            return z(d.id);
        });

    dimension.append("text")
        .datum(function (d) {
            return {id: d.id, data: d.data[d.data.length - 1]};
        })
        .attr("transform", function (d) {
            return "translate(" + x(d.data.date) + "," + y(d.data[yDimension]) + ")";
        })
        .attr("x", 3)
        .attr("dy", "0.35em")
        .style("font", "10px sans-serif")
        .text(function (d) {
            return d.id;
        });

}


function orderSourceChart(dataArr, axisXFormat) {
    var svg = d3.select("#order-source")

    svg.selectAll("*").remove();

        var margin = {top: 20, right: 80, bottom: 30, left: 50},
        width = svg.attr("width") - margin.left - margin.right,
        height = svg.attr("height") - margin.top - margin.bottom,
        g = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");


    var x = d3.scaleTime().range([0, width]),
        y = d3.scaleLinear().range([height, 0]),
        z = d3.scaleOrdinal(d3.schemeCategory10);

    var line = d3.line()
        // .curve(d3.curveBasis)
        .curve(d3.curveCardinal.tension(0.8))
        .x(function (d) {
            return x(d.date);
        })
        .y(function (d) {
            return y(d.amount);
        });



    x.domain([
        d3.min(dataArr, function (c) {
            return d3.min(c.data, function (d) {
                return d.date;
            });
        }),
        d3.max(dataArr, function (c) {
            return d3.max(c.data, function (d) {
                return d.date;
            });
        })
    ]);



    y.domain([
        d3.min(dataArr, function (c) {
            return d3.min(c.data, function (d) {
                return d.amount;
            });
        }),
        d3.max(dataArr, function (c) {
            return d3.max(c.data, function (d) {
                return d.amount;
            });
        })
    ]);

    z.domain(dataArr.map(function (c) {
        return c.id;
    }));


    var axisX = d3.axisBottom(x)
            .ticks(dataArr[0].data.length)
            .tickFormat((d) => {
                return d3.timeFormat(axisXFormat.format)(d);
});


    var axisY = d3.axisLeft(y)
            .tickFormat((d) => {
            return d / 1000000;
    // dailyData.find((d) => d.week == week).label
});


    g.append("g")
        .attr("class", "axis axis--x")
        .attr("transform", "translate(0," + height + ")")
        .call(axisX)
        .append("text")
        .attr("x", x(dataArr[0].data[dataArr[0].data.length -1].date) + 20)
        .attr("y", 10)
        .attr("dy", "0.71em")
        .attr("fill", "#000")
        .text("(" + axisXFormat.unit + ")");

    g.append("g")
        .attr("class", "axis axis--y")
        .call(axisY)
        .append("text")
        // .attr("transform", "rotate(-90)")
        .attr("x", 60)
        .attr("y", 6)
        .attr("dy", "0.71em")
        .attr("fill", "#000")
        .text("營收 (百萬)");

    var dimension = g.selectAll(".amount")
        .data(dataArr)
        .enter().append("g")
        .attr("class", 'amount');

    dimension.append("path")
    // .attr("class", "line")
        .attr("d", function (d) {
            return line(d.data);
        })
        .attr("fill", "none")
        .attr("stroke", function (d) {
            return z(d.id);
        });

    dimension.append("text")
        .datum(function (d) {
            return {id: d.id, data: d.data[d.data.length - 1]};
        })
        .attr("transform", function (d) {
            return "translate(" + x(d.data.date) + "," + y(d.data.amount) + ")";
        })
        .attr("x", 3)
        .attr("dy", "0.35em")
        .style("font", "10px sans-serif")
        .text(function (d) {
            return d.id;
        });

}


function orderCustomerChart(data, axisXFormat) {
    var svg = d3.select("#order-customer")
    svg.selectAll("*").remove();

    var margin = {top: 20, right: 20, bottom: 30, left: 40},
        width = +svg.attr("width") - margin.left - margin.right,
        height = +svg.attr("height") - margin.top - margin.bottom,
        g = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    // svg.attr("style","background:#e2e2e2")

    var x = d3.scaleBand()
        .rangeRound([0, width])
        .paddingInner(0.35)
        // .paddingOuter(0.1)
        .align(0.1);

    var y1 = d3.scaleLinear()
        .rangeRound([height, 0]);

    var y2 = d3.scaleLinear()
        .rangeRound([height, 0]);

    var y3 = d3.scaleLinear()
        .rangeRound([height, 0]);

    // var z = d3.scaleOrdinal()
    //     .range(["#ff0080", "#0000ff","#ff8000"]);
    var z = d3.scaleOrdinal(d3.schemeCategory10);

    var axisX = d3.axisBottom(x)
            .ticks(data.length)
            .tickFormat((d) => {
            return d3.timeFormat(axisXFormat.format)(d);
            });

    x.domain(data.map(function (d) {
        return d.date;
    }));

    y1.domain([0, d3.max(data, function (d) {
        return d.amount;
    })]).nice();

    y2.domain([0, d3.max(data, function (d) {
        return d.customer;
    })]).nice();

    y3.domain([0, d3.max(data, function (d) {
        return d.average;
    })]).nice();

    z.domain(["amount", "average"]);


    var line = d3.line()
    // .curve(d3.curveBasis)
        .x(function (d) {

            return x(d.date) + x.bandwidth() / 2;
        })
        .y(function (d) {
            return y1(d.amount);
        });

    var averageLine = d3.line()
    // .curve(d3.curveBasis)
        .x(function (d) {
            return x(d.date) + x.bandwidth() / 2;
        })
        .y(function (d) {
            return y3(d.average);
        });


    g.append("g")
        .selectAll("rect")
        .data(data)
        .enter().append("rect")
        .attr("fill", '#ff0080')
        .attr("x", function (d) {
            return x(d.date);
        })
        .attr("y", function (d) {
            return y2(d.customer);
        })
        .attr("height", function (d) {
            return y2(0) - y2(d.customer);
        })
        .attr("width", x.bandwidth());



    var y1g = g.append("g")
        .datum(data)

        y1g.append("path")
        .attr("fill", "none")
        .attr("stroke", z("amount"))
        .attr("d", line)

        y1g.append("text")
        .datum(function (d) {
            return {id: '營收', data: d[d.length - 1]};
        })
        .attr("transform", function (d) {
            return "translate(" + x(d.data.date) + "," + y1(d.data.amount) + ")";
        })
        .attr("x", 3)
        .attr("dy", "0.35em")
        .style("font", "10px sans-serif")
        .text(function (d) {
            return d.id;
        });





    var y3g = g.append("g")
        .datum(data);

        y3g.append("path")
        .attr("fill", "none")
        .attr("stroke", z("average"))
        .attr("d", averageLine)


    y3g.append("text")
        .datum(function (d) {
            return {id: '客單價', data: d[d.length - 1]};
        })
        .attr("transform", function (d) {
            return "translate(" + x(d.data.date) + "," + y3(d.data.average) + ")";
        })
        .attr("x", 3)
        .attr("dy", "0.35em")
        .style("font", "10px sans-serif")
        .text(function (d) {
            return d.id;
        });


    //
    // g.append("g")
    //     .selectAll("g")
    //     .data(d3.stack().keys(keys)(data))
    //     .enter().append("g")
    //     .attr("fill", function (d) {
    //         return z(d.key);
    //     })
    //     .selectAll("rect")
    //     .data(function (d) {
    //         return d;
    //     })
    //     .enter().append("rect")
    //     .attr("x", function (d) {
    //         return x(d.data.State);
    //     })
    //     .attr("y", function (d) {
    //         return y(d[1]);
    //     })
    //     .attr("height", function (d) {
    //         return y(d[0]) - y(d[1]);
    //     })
    //     .attr("width", x.bandwidth());

    g.append("g")
        .attr("class", "axis")
        .attr("transform", "translate(0," + height + ")")
        .call(axisX)
        .append("text")
        .attr("x", x(data[data.length - 1].date))
        .attr("y", 20)
        .attr("dy", "0.71em")
        .attr("fill", "#000")
        .text("(" + axisXFormat.unit + ")");

    // g.append("g")
    //     .attr("class", "axis")
    //     .call(d3.axisLeft(y2).ticks(null, "s"))
    //     .append("text")
    //     .attr("x", 2)
    //     .attr("y", y2(y2.ticks().pop()) + 0.5)
    //     .attr("dy", "0.32em")
    //     .attr("fill", "#000")
    //     .attr("font-weight", "bold")
    //     .attr("text-anchor", "start")
    //     .text("Population");


}