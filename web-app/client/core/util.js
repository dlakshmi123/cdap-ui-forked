/*
 * Utilities
 */

define([], function () {

	Em.debug('Loading Util');

	$.timeago = $.timeago || function () {};
	$.timeago.settings.strings.seconds = '%d seconds';
	$.timeago.settings.strings.minute = 'About a minute';
	$.timeago.settings.refreshMillis = 0;

	Date.prototype.ISO8601 = function (date) {
		date = date || this;
		var pad_two = function(n) {
			return (n < 10 ? '0' : '') + n;
		};
		var pad_three = function(n) {
			return (n < 100 ? '0' : '') + (n < 10 ? '0' : '') + n;
		};
		return [
			date.getUTCFullYear(), '-',
			pad_two(date.getUTCMonth() + 1), '-',
			pad_two(date.getUTCDate()), 'T',
			pad_two(date.getUTCHours()), ':',
			pad_two(date.getUTCMinutes()), ':',
			pad_two(date.getUTCSeconds()), '.',
			pad_three(date.getUTCMilliseconds()), 'Z'
		].join('');
	};

	var Util = Em.Object.extend({

		/**
     * Looks up unique id for a record or generates it and adds it to index.
     * @param  {string} recordName.
     * @return {string} id unique id for record.
     */
		generateUid: function () {
			return Math.random().toString(36).substr(2,9);
		},

		parseQueryString: function (path) {

			var result = {};
			var qs = path.split('?')[1];
			if (!qs) {
				return result;
			}

			var pairs = qs.split('&'), pair;
			var i = pairs.length;
			while (i--) {
				pair = pairs[i].split('=');
				result[pair[0]] = pair[1];
			}

			return result;

		},

		enc: function (string) {

			return encodeURIComponent(string).replace(/\./g, '%2E');

		},

		Cookie: $.cookie,

		Upload: Em.Object.create({

			processing: false,
			resource_identifier: null,
			fileQueue: [],
			entityType: null,

			configure: function () {

				function ignoreDrag(e) {
					e.originalEvent.stopPropagation();
					e.originalEvent.preventDefault();
				}

				var self = this;
				var element = $('body');

				function drop (e) {
					ignoreDrag(e);

					C.Util.interrupt();

					if (!C.Util.Upload.processing) {
						var dt = e.originalEvent.dataTransfer;
						C.Util.Upload.sendFiles(dt.files, self.get('entityType'));
						$('#far-upload-alert').hide();
					}
				}

				element.bind('dragover', function (e) {

					ignoreDrag(e);
					$('#drop-hover').fadeIn();

				})
				.bind('dragover', ignoreDrag)
				.bind('drop', drop)
				.bind('keydown', function (e) {
					if (e.keyCode === 27) {
						$('#drop-hover').fadeOut();
					}
				});

			},

			__sendFile: function () {

				var file = this.fileQueue.shift();
				if (file === undefined) {
					C.Modal.show("Deployment Error", 'No file specified.');
					$('#drop-hover').fadeOut(function () {
						$('#drop-label').show();
						$('#drop-loading').hide();
					});
					return;
				}

				var xhr = new XMLHttpRequest();
				var uploadProg = xhr.upload || xhr;

				uploadProg.addEventListener('progress', function (e) {

					if (e.type === 'progress') {
						var pct = Math.round((e.loaded / e.total) * 100);
						$('#far-upload-status').html(pct + '% Uploaded...');
					}

				}, false);

				xhr.open('POST', '/upload/' + file.name, true);
				xhr.setRequestHeader("Content-type", "application/octet-stream");
				xhr.setRequestHeader("X-Archive-Name", file.name);
				xhr.send(file);

				function checkDeployStatus () {

					$.getJSON('/upload/status', function (status) {

						switch (status.code) {
							case 4:
								C.Modal.show("Deployment Error", status.message);
								$('#drop-hover').fadeOut(function () {
									$('#drop-label').show();
									$('#drop-loading').hide();
								});
								break;
							case 5:
								$('#drop-hover').fadeOut();
								window.location.reload();
								break;
							default:
								checkDeployStatus();
						}

					});

				}

				xhr.onreadystatechange = function () {

					if (xhr.readyState === 4) {

						if (xhr.statusText === 'OK') {
							checkDeployStatus();

						} else {
							C.Modal.show("Deployment Error", xhr.responseText);
							$('#drop-hover').fadeOut(function () {
								$('#drop-label').show();
								$('#drop-loading').hide();
							});

						}

					}
				};
			},

			sendFiles: function (files, type) {

				this.set('entityType', type);

				this.fileQueue = [];
				for (var i = 0; i < files.length; i ++) {
					this.fileQueue.push(files[i]);
				}

				if (files.length > 0) {
					this.__sendFile();
				}
			}
		}),

		updateCurrents: function (models, http, controller, offset) {

			var j, k, metrics, map = {};
			var queries = [];

			models = models.filter(function (item) {
				return item !== undefined;
			});

			for (j = 0; j < models.length; j ++) {

				metrics = Em.keys(models[j].get('currents') || {});

				for (var k = 0; k < metrics.length; k ++) {

						var metric = models[j].get('currents').get(metrics[k]);
						queries.push(metric.path + '?start=now-' + (offset || 5) + 's&count=1&interpolate=step');
						map[metric.path] = models[j];

				}

			}

			if (queries.length) {
				http.post('metrics', queries, function (response) {
					controller.set('aggregatesCompleted', true);
					if (response.result) {

						var result = response.result;

						var i, k, data, path, label;
						for (i = 0; i < result.length; i ++) {
							path = result[i].path.split('?')[0];
							label = map[path].get('currents')[C.Util.enc(path)].value;

							if (label) {
								map[path].setMetric(label, result[i].result.data[0].value);
							}
						}
					}
				});
			} else {
				controller.set('aggregatesCompleted', true);
			}

		},

		updateAggregates: function (models, http, controller) {

			var j, k, metrics, map = {};
			var queries = [];

			var max = 60;

			models = models.filter(function (item) {
				return item !== undefined;
			});

			for (j = 0; j < models.length; j ++) {

				metrics = Em.keys(models[j].get('aggregates') || {});

				for (var k = 0; k < metrics.length; k ++) {

						var metric = models[j].get('aggregates').get(metrics[k]);
						queries.push(metric.path + '?aggregate=true');

						map[metric.path] = models[j];

				}

			}

			if (queries.length) {
				http.post('metrics', queries, function (response) {
					controller.set('aggregatesCompleted', true);
					if (response.result) {

						var result = response.result;

						var i, k, data, path, label;
						for (i = 0; i < result.length; i ++) {
							path = result[i].path.split('?')[0];

							if (map[path].get('aggregates')[C.Util.enc(path)]) {

								label = map[path].get('aggregates')[C.Util.enc(path)].value;
								if (label) {
									map[path].setMetric(label, result[i].result.data);
								}

							}
						}
					}
				});
			} else {
				controller.set('aggregatesCompleted', true);
			}

		},

		updateTimeSeries: function (models, http, controller) {

			var j, k, metrics, count, map = {};
			var queries = [];

			var start = 'now-' + (C.__timeRange + C.METRICS_BUFFER) + 's';
			var end = 'now-' + C.METRICS_BUFFER + 's';
			var max = C.SPARKLINE_POINTS;

			var path;

			models = models.filter(function (item) {
				return item !== undefined;
			});

			for (j = 0; j < models.length; j ++) {

				metrics = Em.keys(models[j].get('timeseries') || {});

				for (var k = 0; k < metrics.length; k ++) {

					var metric = models[j].get('timeseries').get(metrics[k]);

					// Check metric is an ember object.
					if (typeof metric === 'object') {
						if (metric) {
							count = max - metric.get('value.length');
							count = count || 1;

						} else {

							metric.set('value', []);
							count = max;

						}

						// Hax. Server treats end = start + count (no downsample yet)
						count = C.__timeRange;
						map[metric.path] = models[j];
						path = metric.path + '?start=' + start + '&end=' + end + '&count=' + count;

						if (metric.interpolate) {
							path += '&interpolate=step';
						}

						queries.push(path);
					}

				}

			}

			if (queries.length) {

				http.post('metrics', queries, function (response) {

					controller.set('timeseriesCompleted', true);
					if (response.result) {

						var result = response.result;

						// Real Hax. Memory comes back in MB.
						var multiplyBy = 1;

						var i, k, data, path;
						for (i = 0; i < result.length; i ++) {

							path = result[i].path.split('?')[0];

							// Real Hax. Memory comes back in MB.
							if (path.indexOf('resources.used.memory') !== -1) {
								multiplyBy = 1024;
							}

							if (!result[i].error) {

								data = result[i].result.data, k = data.length;

								while(k --) {
									data[k] = data[k].value * multiplyBy;
								}

								map[path].set('timeseries.' + C.Util.enc(path) + '.value', data);

								/*
								SOMEDAY: Use count to reduce traffic.

								var mapped = map[path].get('timeseries');
								var ts = mapped.get(path);

								ts.shift(data.length);
								ts = ts.concat(data);

								mapped.set(path, ts);
								*/

							}

						}
					}

				});
			} else {
				controller.set('timeseriesCompleted', true);
			}

		},

		updateRates: function (models, http, controller) {

			var j, k, metrics, count, map = {};
			var queries = [];

			var max = 1, start;
			var now = new Date().getTime();
			var count = 5;

			start = now - ((count + 2) * 1000);
			start = Math.floor(start / 1000);

			models = models.filter(function (item) {
				return item !== undefined;
			});

			for (j = 0; j < models.length; j ++) {

				metrics = Em.keys(models[j].get('rates') || {});

				for (var k = 0; k < metrics.length; k ++) {

					var metric = models[j].get('rates').get(metrics[k]);

					map[metric.path] = models[j];
					queries.push(metric.path + '?start=now-10s&end=now-5s&count=5');

				}

			}

			if (queries.length) {

				http.post('metrics', queries, function (response) {

					controller.set('ratesCompleted', true);
					if (response.result) {

						var result = response.result;

						var i, k, data, path, label;
						for (i = 0; i < result.length; i ++) {

							path = result[i].path.split('?')[0];

							if (!result[i].error) {

								data = result[i].result.data, k = data.length;

								// Averages over the values returned (count)
								var total = 0;
								while(k --) {
									total += data[k].value;
								}

								if (map[path].get('rates')[C.Util.enc(path)]) {

									label = map[path].get('rates')[C.Util.enc(path)].value;
									if (label) {
										map[path].setMetric(label, total / data.length);
									}
								}

							}
						}
					}

				});
			} else {
				controller.set('ratesCompleted', true);
			}

		},

		sparkline: function (widget, data, w, h, percent, shade) {

			var allData = [], length = 0;
			for (var i in this.series) {
				allData = allData.concat(this.series[i]);
				if (this.series[i].length > length) {
					length = this.series[i].length;
				}
			}
			var max = d3.max(allData) || 9;
			var min = d3.min(allData) || -1;
			var extend = Math.round(w / data.length);

			var margin = 5;
			var yBuffer = 0.0;
			var y, x;

			x = d3.scale.linear();//.domain([0, data.length]).range([0, w]);
			y = d3.scale.linear();

			var vis = widget
				.append("svg:svg")
				.attr('width', '100%')
				.attr('height', '100%')
				.attr('preserveAspectRatio', 'none');

			var g = vis.append("svg:g");
			var line = d3.svg.line().interpolate("monotone")
				.x(function(d,i) { return x(i); })
				.y(function(d) { return y(d); });

			if (percent || shade) {
				var area = d3.svg.area()
					.x(line.x())
					.y1(line.y())
					.y0(y(0));
				g.append("svg:path").attr('class', 'sparkline-area').attr("d", area(data));
			}

			g.append("svg:path").attr('class', 'sparkline-data').attr("d", line(data));

			return {
				g: g,
				percent: percent,
				shade: shade,
				series: {}, // Need to store to track data boundaries
				update: function (name, data) {

					this.series[name] = data;

					var allData = [], length = 0;
					for (var i in this.series) {
						allData = allData.concat(this.series[i]);
						if (this.series[i].length > length) {
							length = this.series[i].length;
						}
					}
					var max = d3.max(allData) || 100;
					var min = d3.min(allData) || 0;
					var extend = Math.round(w / data.length);

					var yBuffer = 0.0;
					var y, x;

					x = d3.scale.linear().domain([0, length]).range([0 - extend, w - extend]);

					if (this.percent) {
						y = d3.scale.linear()
							.domain([100, 0])
							.range([margin, h - margin]);
					} else {
						if ((max - min) === 0) {
							if (data[0]) {
								max = data[0] + data[0] * 0.1;
								min = data[0] - data[0] * 0.1;
							} else {
								max = 10;
								min = 0;
							}
						}
						y = d3.scale.linear()
							.domain([max + (max * yBuffer), min - (min * yBuffer)])
							.range([margin, h - margin]);
					}


					var line = d3.svg.line().interpolate("monotone")
						.x(function(d,i) { return x(i); })
						.y(function(d) { return y(d); });

					if (this.percent || this.shade) {
						var area = d3.svg.area().interpolate("monotone")
							.x(line.x())
							.y1(line.y())
							.y0(y(-100));

						this.g.selectAll("path.sparkline-area")
							.data([data])
							.attr("transform", "translate(" + x(0) + ")")
							.attr("d", area)
							.transition()
							.ease("linear")
							.duration(C.POLLING_INTERVAL)
							.attr("transform", "translate(" + x(-(C.POLLING_INTERVAL / 1000)) + ")");
					}

					this.g.selectAll("path.sparkline-data")
						.data([data])
						.attr("transform", "translate(" + x(0) + ")")
						.attr("d", line)
						.transition()
						.ease("linear")
						.duration(C.POLLING_INTERVAL)
						.attr("transform", "translate(" + x(-(C.POLLING_INTERVAL / 1000)) + ")");

				}
			};
		},

		number: function (value) {

			value = Math.abs(value);

			if (value > 1000000000) {
				var digits = 3 - (Math.round(value / 1000000000) + '').length;
				digits = digits < 0 ? 2 : digits;
				value = value / 1000000000;
				var rounded = Math.round(value * Math.pow(10, digits)) / Math.pow(10, digits);
				return [rounded, 'B'];

			} else if (value > 1000000) {
				var digits = 3 - (Math.round(value / 1000000) + '').length;
				digits = digits < 0 ? 2 : digits;
				value = value / 1000000;
				var rounded = Math.round(value * Math.pow(10, digits)) / Math.pow(10, digits);
				return [rounded, 'M'];

			} else if (value > 1000) {
				var digits = 3 - (Math.round(value / 1000) + '').length;
				digits = digits < 0 ? 2 : digits;
				value = value / 1000;
				var rounded = Math.round(value * Math.pow(10, digits)) / Math.pow(10, digits);
				return [rounded, 'K'];

			}

			var digits = 3 - (value + '').length;
			digits = digits < 0 ? 2 : digits;
			var rounded = Math.round(value * Math.pow(10, digits)) / Math.pow(10, digits);

			return [rounded, ''];

		},
		numberArrayToString: function(value) {
			return this.number(value).join('');
		},

		bytes: function (value) {

			if (value >= 1073741824) {
				value /= 1073741824;
				return [((Math.round(value * 100) / 100)), 'GB'];
			} else if (value >= 1048576) {
				value /= 1048576;
				return [((Math.round(value * 100) / 100)), 'MB'];
			} else if (value >= 1024) {
				value /= 1024;
				return [((Math.round(value * 10) / 10)), 'KB'];
			}

			return [value, 'B'];
		},

		interrupt: function () {

			$('#drop-border').addClass('hidden');

			$('#drop-label').hide();
			$('#drop-loading').show();
			$('#drop-hover').show();

		},

		proceed: function (done) {

			$('#drop-hover').fadeOut(function () {

				$('#drop-border').removeClass('hidden');

				$('#drop-label').show();
				$('#drop-loading').hide();
				if (typeof done === 'function') {
					done();
				}
			});

		},

		/**
		 * Pauses the thread for a predetermined amount of time, useful whenever execution needs to be
		 * delayed.
		 * @param  {number} milliseconds
		 */
		threadSleep: function (milliseconds) {
			var time = new Date().getTime() + milliseconds;
			while (new Date().getTime() <= time) {

				$.noop();

			}
		},

		reset: function () {

			C.Modal.show(
				"Reset Reactor",
				"You are about to DELETE ALL CONTINUUITY DATA on your Reactor." +
					" Are you sure you would like to do this?",
				function () {

					C.Util.interrupt();


					$.ajax({
						url: '/unrecoverable/reset',
						type: 'POST'
					}).done(function (response, status) {

						if (response === "OK") {
							window.location = '/';
						} else {
							C.Util.proceed(function () {
								C.Modal.show("Reset Error", response);
							});
						}

					}).fail(function (xhr, status, error) {

						C.Util.proceed(function () {

							setTimeout(function () {
								C.Modal.show("Reset Error", xhr.responseText);
							}, 500);

						});
					});

				});
			return false;
		}
	});

	return Util.create();

});