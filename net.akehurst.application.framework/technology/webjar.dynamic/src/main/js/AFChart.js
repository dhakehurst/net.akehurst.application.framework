/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
 
define([
	"jquery",
	"chartjs",
], function($, chartjs) {

	// element should be a div with class='chart'
	function AFChart(elementId, data) {
		this.elementId = elementId
		this.element = document.getElementById(elementId)
		this.chart = {}
		this.chartContext = {}
		this.create(data)
	}

	AFChart.prototype.create = function(data) {
		let self = this
		try {
			let width = $(this.element).width()
			let height = $(this.element).height()
			let canvas = $("<canvas></canvas>").appendTo($(this.element))
			this.chartContext = canvas[0].getContext("2d")
			let chatOpts = $.extend(data.options, {
				responsive:true,
				//maintainAspectRatio: false,
				onResize: function(chart, size) {
					chart.update()
				}
			})
			$(this.element).css('position','relative')
			this.chart = new Chart(this.chartContext, {type:data.type, data: data.data, options:chatOpts})
		    
		} catch (err) {
			console.log("Error: "+err.message)
			return ""
		}
	}
	
	return AFChart
})