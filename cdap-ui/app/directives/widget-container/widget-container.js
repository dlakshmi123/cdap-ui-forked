/*
 * Copyright © 2015 Cask Data, Inc.
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

angular.module(PKG.name + '.commons')
  .directive('widgetContainer', function($compile, $window, WidgetFactory) {
    return {
      restrict: 'A',
      scope: {
        name: '=',
        model: '=',
        myconfig: '=',
        properties: '=',
        widgetDisabled: '='
      },
      replace: false,
      link: function (scope, element) {
        var angularElement,
            widget,
            fieldset;
        if (WidgetFactory.registry[scope.myconfig.widget]) {
          widget = WidgetFactory.registry[scope.myconfig.widget];
        } else {
          widget = WidgetFactory.registry['__default__'];
        }

        fieldset = angular.element('<fieldset></fieldset>');
        fieldset.attr('ng-disabled', scope.widgetDisabled);

        angularElement = angular.element(widget.element);
        angular.forEach(widget.attributes, function(value, key) {
          angularElement.attr(key, value);
        });

        fieldset.append(angularElement);
        element.append(fieldset);

        element.removeAttr('widget-container');
        $compile(element)(scope);
      }
    };

  });
