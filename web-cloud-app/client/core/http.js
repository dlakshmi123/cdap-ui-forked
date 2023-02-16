/*
 * HTTP Resource
 */

define([], function () {

	Em.debug('Loading HTTP Resource');

	/*
	 * Joins the arguments as a path string.
	 * e.g. HTTP.get('metrics', 1, 2, 3) => GET /metrics/1/2/3 HTTP/1.1
	 */
	function findPath(args) {
		var path = [];
		for (var i = 0; i < args.length; i ++) {
			if (typeof args[i] === 'string' || typeof args[i] === 'number') {
				path.push(args[i]);
			}
		}
		return '/' + path.join('/');
	}

	/**
	 * Iterates over arguments to find an object that should be mapped as a query string. This is not
	 * recursive and reaches only 1 level depth of recursion.
	 * eg: HTTP.get('metrics', 1, 2, {'count': 'total', 'foo': 'bar'}) => count=total&foo=bar
	 * @param {Array} args arguments.
	 * @returns {string} query string part of url.
	 */
	function findQueryString(args) {
		var query = {};

		args = Array.prototype.slice.call(args);
		for (var i = 0, len = args.length; i < len; i++) {
			if(Object.prototype.toString.call(args[i]) === "[object Object]") {
				$.extend(query, args[i]);
			}
		}

		return $.param(query);
	}

	/*
	 * Finds the object argument based on the last argument.
	 */
	function findObject(args) {
		var object = args[args.length - 1];
		if (typeof object === 'function') {
			object = args[args.length - 2];
		}
		return (!object || typeof object === 'string' ? null : object);
	}

	/*
	 * Finds the callback argument based on the last argument.
	 */
	function findCallback(args) {
		var callback = args[args.length - 1];
		return (typeof callback === 'function' ? callback : function () {
			Em.debug('No callback provided for HTTP response.');
		});
	}

	var Resource = Em.Object.extend({

		get: function () {

			var path = findPath(arguments);
			var queryString = findQueryString(arguments);
			var callback = findCallback(arguments);
			path = queryString ? path + '?' + queryString : path;

			$.getJSON(path, callback).fail(function (req) {

				var error = JSON.parse(req.responseText);
				if (error.fatal) {

					$('#warning').html('<div>' + error.fatal + '</div>').show();

				}

			});

		},

		rest: function () {

			var args = [].slice.call(arguments);
			args.unshift('rest');
			this.get.apply(this, args);

		},

		post: function () {

			var path = findPath(arguments);
			var object = findObject(arguments);
			var callback = findCallback(arguments);

			$.ajax({
				url: path,
				data: JSON.stringify(object),
				type: "POST",
				contentType: "application/json"
			}).done(function (response, status) {

				if (response.error && response.error.fatal) {
					$('#warning').html('<div>' + response.error.fatal + '</div>').show();
				} else {
					callback(response, status);
				}

			}).fail(function (xhr) {

				$('#warning').html('<div>Encountered a connection problem.</div>').show();

			});

		},

		rpc: function () {

			var args = [].slice.call(arguments);
			args.unshift('rpc');

			var object = args[args.length - 2];

			if (typeof object === 'object' && object.length) {
				args[args.length - 2] = object;
			}

			this.post.apply(this, args);

		},

		"delete": function () {

			var path = findPath(arguments);
			var callback = findCallback(arguments);
			this.post(path + '?_method=DELETE', callback);

		}

	});

	Resource.reopenClass({
		type: 'HTTP',
		kind: 'Resource'
	});

	return Resource;

});
