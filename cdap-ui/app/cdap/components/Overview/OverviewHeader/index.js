/*
 * Copyright © 2017 Cask Data, Inc.
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

import React, {PropTypes, Component} from 'react';
import classnames from 'classnames';
import {Link} from 'react-router';
require('./OverviewHeader.scss');

export default class OverviewHeader extends Component {
  constructor(props) {
    super(props);
    this.state = {
      successMessage: this.props.successMessage
    };
  }
  componentWillReceiveProps(nextProps) {
    if (nextProps.successMessage !== this.state.successMessage) {
      this.setState({
        successMessage: nextProps.successMessage
      }, () => {
        setTimeout(() => {
          this.setState({
            successMessage: null
          });
        }, 3000);
      });
    }
  }
  render() {
    return (
      <div className="overview-header">
        {
          this.state.successMessage ?
            <div className="overview-header success-message">
              <h5>
                <span>
                  {
                    this.state.successMessage
                  }
                </span>
              </h5>
            </div>
          :
            null
        }
        <div className="header">
          <i className={classnames("fa", this.props.icon)} />
          <h4>{this.props.title}</h4>
        </div>
        {
          this.props.linkTo ?
            <Link
              className="link-to-detail"
              to={this.props.linkTo}
            >
              View Details
            </Link>
          :
            null
        }
        {
          this.props.onClose ?
            <span
              className="fa fa-times"
              onClick={this.props.onClose}
            >
            </span>
          :
            null
        }
      </div>
    );
  }
}
OverviewHeader.propTypes = {
  icon: PropTypes.string,
  title: PropTypes.string,
  linkTo: PropTypes.object,
  onClose: PropTypes.func,
  successMessage: PropTypes.string
};
