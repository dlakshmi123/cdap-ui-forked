/*
 * Copyright © 2020 Cask Data, Inc.
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

import React, { cloneElement } from 'react';
import withStyles, { WithStyles, StyleRules } from '@material-ui/core/styles/withStyles';

const styles = (): StyleRules => {
  return {
    grid: {
      overflowY: 'auto',
    },
  };
};

interface ITableProps extends WithStyles<typeof styles> {
  columnTemplate: string;
  dataCy?: string;
}

const TableView: React.FC<React.PropsWithChildren<ITableProps>> = ({
  classes,
  children,
  columnTemplate,
  dataCy,
}) => {
  const childrenClone = React.Children.map(children, (child: React.ReactElement<any>) => {
    return cloneElement(child, { columnTemplate });
  });

  return (
    <div className={classes.grid} data-cy={dataCy} data-testid={dataCy}>
      {childrenClone}
    </div>
  );
};

const Table = withStyles(styles)(TableView);
export default Table;
