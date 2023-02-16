/*
 * Main entry point for Reactor UI
 * Defines routes and attaches mocks
 */

define (['core/application'], function (Application) {

	/*
	 * Determine whether to swap out specific components with mocks.
	 */
    var mocks = window.location.search.split('?')[1];


	if (mocks) {
		mocks = mocks.split('=')[1];
		if (mocks) {
			mocks = mocks.split(',');
		} else {
			mocks = null;
		}
	} else {
		mocks = null;
	}

	/*
	 * Inject requested mocks into our controllers.
	 */
	if (mocks) {

		Em.Application.initializer({
			name: "mocks",
			before: "resources",

			initialize: function(container, application) {

				var i = mocks.length;
				while (i--) {
					C.__mocked[mocks[i]] = true;
					mocks[i] = 'mocks/' + mocks[i].toLowerCase();
				}

				/*
				 * Note: This is async. The 'resources' initializer is not.
				 */
				require(mocks, function () {

					mocks = [].slice.call(arguments, 0);

					var i = mocks.length, type, resource;
					while (i--) {

						type = mocks[i].type;
						container.optionsForType(type, { singleton: true });
						container.register(type + ':main', mocks[i]);
						container.typeInjection('controller', type, type + ':main');

						/*
						 * Check Application-level event handlers on the resource.
						 * E.g. Socket.on('connect');
						 */
						if (typeof C.__handlers[type] === 'object') {

							resource = container.lookup(type + ':main');
							for (var event in C.__handlers[type]) {
                if (C.__handlers[type].hasOwnProperty(event)) {
                    resource.on(event, C.__handlers[type][event]);
                }
							}
							if (typeof resource.connect === 'function') {
								resource.connect();
							}

						}
					}
				});
			}
		});

	}

	/*
	 * Instantiate the Application.
	 */
	window.C = Application.create();

	/*
	 * Temporary hold for Tree controls. (Resource View)
	 */
	C.TreeBranchController = Ember.ObjectController.extend({});
	C.register('controller:treeBranch', C.TreeBranchController, { singleton: false });

	C.TreeBranchView = Ember.View.extend({
		tagName: 'ul',
		templateName: 'tree-branch',
		classNames: ['tree-branch']
	});

	C.TreeNodeController = Ember.ObjectController.extend({
		isExpanded: false,
		toggle: function() {
			this.set('isExpanded', !this.get('isExpanded'));
		},
		click: function() {
			console.log('Clicked: ' + this.get('text'));
		}
	});
	C.register('controller:treeNode', C.TreeNodeController, { singleton: false });

	C.TreeNodeView = Ember.View.extend({
		tagName: 'li',
		templateName: 'tree-node',
		classNames: ['tree-node']
	});

	/*
	 * The following define the routes in use by the application.
	 * Templates are referred to by resource name and inserted automatically.
	 * Models are determined by the dynamic route and loaded automatically.
	 */
	C.Router.map(function() {

		this.resource('Overview', { path: '/overview' } );

		this.resource('Resources', { path: '/resources' } );

		this.resource('App', { path: '/apps/:app_id' } );

		this.resource('Streams', { path: '/streams' });
		this.resource('Stream', { path: '/streams/:stream_id' });

		this.resource('Flows', { path: '/flows' });
		this.resource('Flow', { path: '/flows/:flow_id' }, function() {

			this.resource('FlowStatus', { path: '/' }, function () {

				// These live in FlowStatus so they can visually overlay the Flow.
				this.route('Flowlet', { path: '/flowlets/:flowlet_id' });
				this.route('Stream', { path: '/streams/:stream_id' });
				this.route('Config', { path: '/config' });

			});

			this.route('Log', { path: '/log' });
			this.route('History', { path: '/history' });

		});

		this.resource('Datasets', { path: '/datasets' });
		this.resource('Dataset', { path: '/datasets/:dataset_id' });

		this.resource('Procedures', { path: '/procedures' });
		this.resource('Procedure', { path: '/procedures/:procedure_id' }, function () {

			this.resource('ProcedureStatus', { path: '/' }, function () {
				// These live in ProcedureStatus so they can visually overlay the Procedure.
				this.route('Config', { path: '/config' });
			});

			this.route('Log', { path: '/log' });

		});

		this.resource('Batch', {path: '/batches/:batch_id'}, function() {

			this.resource('BatchStatus', { path: '/' }, function () {
				// These live in BatchStatus so they can visually overlay the Batch Job.
				this.route('Config', { path: '/config' });
			});
			this.route('Log', { path: '/log'});

		});

		this.resource('Workflow', {path: '/workflows/:workflow_id'}, function () {
			this.resource('WorkflowStatus', {path: '/'}, function () {
				this.route('Config', { path: '/config'});
			});
			this.route('History', { path: '/history' });
		});

		this.route('Analyze', { path: '/analyze' });

		this.route("PageNotFound", { path: "*:" });

	});

	function modelFinder (params) {
		for (var key in params) {
      if (params.hasOwnProperty(key)) {
        /*
         * Converts e.g. 'app_id' into 'App', 'flow_id' into 'Flow'
         */
        var type = key.charAt(0).toUpperCase() + key.slice(1, key.length - 3);
        /*
         * Finds type and injects HTTP
         */
        if (type in C) {
          return C[type].find(params[key],
            this.controllerFor('Application').HTTP);
        }
      }
		}
	}

	/*
	 * This is a basic route handler that others can extend from to reduce duplication.
	 */
	var basicRouter = Ember.Route.extend({
		/*
		 * Override to load the Controller once the Route has been activated.
		 */
		setupController: function(controller, model) {
			controller.set('model', model);
			controller.load();
		},
		/*
		 * Override to unload the Controller once the Route has been deactivated.
		 */
		deactivate: function () {
			this.controller.unload();
		},
		/*
		 * Override to load a model based on parameter name and inject HTTP resource.
		 */
		model: modelFinder

	});

	/*
	 * The following define the actual route handlers.
	 */
	$.extend(C, {

		ApplicationRoute: basicRouter.extend(),

		IndexRoute: Ember.Route.extend({
      redirect: function() {
        this.transitionTo('Overview');
      }
    }),

		OverviewRoute: basicRouter.extend(),

		ResourcesRoute: basicRouter.extend(),

		AppRoute: basicRouter.extend(),

		StreamRoute: basicRouter.extend(),

		/*
		 * Ensures that the HTTP injection is handled properly (see basicRouter)
		 */
		FlowRoute: Ember.Route.extend({
			model: modelFinder
		}),

		FlowStatusRoute: basicRouter.extend({
			model: function () {
				return this.modelFor('Flow');
			}
		}),

		/*
		 * This will use the FlowLogController with the RunnableLog template.
		 * FlowLogController extends RunnableLogController.
		 */
		FlowLogRoute: basicRouter.extend({
			model: function () {
				return this.modelFor('Flow');
			},
			renderTemplate: function () {
				this.render('Runnable/Log');
			}
		}),

		FlowHistoryRoute: basicRouter.extend({
			model: function () {
				return this.modelFor('Flow');
			}
		}),

		FlowStatusFlowletRoute: basicRouter.extend({
			model: function (params) {
				// See FlowletController to see how we get the full Flowlet model.
				return C.Flowlet.create({ 'name': params.flowlet_id });
			}
		}),

		FlowStatusStreamRoute: basicRouter.extend(),

		/*
		 * This will use the FlowStatusConfigController with the RunnableConfig template.
		 * FlowStatusConfigController extends RunnableConfigController.
		 */
		FlowStatusConfigRoute: basicRouter.extend({
			renderTemplate: function () {
				this.render('Runnable/Config');
			}
		}),

		/*
		 * Ensures that the model is handled properly (see basicRouter)
		 */
		BatchRoute: Ember.Route.extend({
			model: modelFinder
		}),

		BatchStatusRoute: basicRouter.extend({
			model: function() {
				return this.modelFor('Batch');
			}
		}),

		BatchLogRoute: basicRouter.extend({
			model: function () {
				return this.modelFor('Batch');
			},
			renderTemplate: function () {
				this.render('Runnable/Log');
			}
		}),

		BatchStatusConfigRoute: basicRouter.extend({
			renderTemplate: function () {
				this.render('Runnable/Config');
			}
		}),

		/*
		 * Ensures that the model is handled properly (see basicRouter)
		 */
		WorkflowRoute: Ember.Route.extend({
			model: modelFinder
		}),

		WorkflowStatusRoute: basicRouter.extend({
			model: function() {
				return this.modelFor('Workflow');
			}
		}),

		WorkflowHistoryRoute: basicRouter.extend({
			model: function () {
				return this.modelFor('Workflow');
			}
		}),

		WorkflowStatusConfigRoute: basicRouter.extend({
			renderTemplate: function () {
				this.render('Runnable/Config');
			}
		}),

		DatasetRoute: basicRouter.extend(),

		/*
		 * Ensures that the HTTP injection is handled properly (see basicRouter)
		 */
		ProcedureRoute: Ember.Route.extend({
			model: modelFinder
		}),

		ProcedureStatusRoute: basicRouter.extend({
			model: function () {
				return this.modelFor('Procedure');
			}
		}),

		ProcedureLogRoute: basicRouter.extend({
			model: function () {
				return this.modelFor('Procedure');
			},
			renderTemplate: function () {
				this.render('Runnable/Log');
			}
		}),

		/*
		 * This will use the FlowStatusConfigController with the RunnableConfig template.
		 * FlowStatusConfigController extends RunnableConfigController.
		 */
		ProcedureStatusConfigRoute: basicRouter.extend({
			renderTemplate: function () {
				this.render('Runnable/Config');
			}
		}),

		AnalyzeRoute: basicRouter.extend(),

		PageNotFoundRoute: Ember.Route.extend()

	});

	/*
	 * Pages for lists of Elements use the List controller.
	 * @param {string} type ['App', 'Stream', 'Flow', ...]
	 */
	function getListHandler(types) {
		return {
			/*
			 * Override to load the Controller once the Route has been activated.
			 */
			setupController: function  () {
				for (var i=0, len=types.length; i<len; i++) {
					this.controllerFor('List').load(types[i]);
				}
			},
			/*
			 * Override the templates to be rendered and where.
			 */
			renderTemplate: function () {
				/*
				 * Render the List Page template (i.e. the header / time selector)
				 */
				this.render('list-page', {
					controller: 'List'
				});
				/*
				 * Render a list type partial into the List Page template
				 */
				for (var i=0, len=types.length; i<len; i++) {
					this.render('_' + types[i].toLowerCase() + 's-list', {
						controller: 'List',
						into: 'list-page'
					});
				}
			},
			/*
			 * Override to unload the Controller once the Route has been deactivated.
			 */
			deactivate: function () {
				this.controllerFor('List').unload();
			}
		};
	}

	$.extend(C, {

		StreamsRoute: Em.Route.extend(getListHandler(['Stream'])),

		FlowsRoute: Em.Route.extend(getListHandler(['Flow', 'Batch', 'Workflow'])),

		WorkflowsRoute: Em.Route.extend(getListHandler(['Workflow'])),

		BatchesRoute: Em.Route.extend(getListHandler(['Batch'])),

		DatasetsRoute: Em.Route.extend(getListHandler(['Dataset'])),

		ProceduresRoute: Em.Route.extend(getListHandler(['Procedure']))

	});

	return C;
});