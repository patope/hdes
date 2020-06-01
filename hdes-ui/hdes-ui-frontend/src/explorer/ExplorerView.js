/*-
 * #%L
 * hdes-dev-app-ui
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import { Component } from 'inferno'
import { ExplorerEntry } from './ExplorerEntry'
import { ExplorerOpenEntries } from './ExplorerOpenEntries'


export class ExplorerView extends Component {
  shouldComponentUpdate(nextProps, nextState) {
    const key = ['explorer'];
    return !this.props.state.getIn(key).equals(nextProps.state.getIn(key))
  }
  render() {
    const { actions, state } = this.props;
    return (
      <aside class='menu explorer-ed'>
        <ul class='menu-list'>
          <li class='explorer-title'>explorer</li>
          <ExplorerOpenEntries actions={actions} state={state} type='oe' name='open editors' />
          <ExplorerEntry actions={actions} state={state} type='FL' name='flows' />
          <ExplorerEntry actions={actions} state={state} type='TG' name='tags' />
          <ExplorerEntry actions={actions} state={state} type='DT' name='decision tables' />
          <ExplorerEntry actions={actions} state={state} type='ST' name='service tasks' />
          <ExplorerEntry actions={actions} state={state} type='MT' name='manual tasks' />
          <ExplorerEntry actions={actions} state={state} type='US' name='users' />
        </ul>
      </aside>
    );
  }
}
