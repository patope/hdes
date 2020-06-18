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

import { ExplorerView } from './../explorer';
import { SearchView } from './../explorer-se';
import { CreateView } from './../explorer-cr';
import { DebugView } from './../explorer-dg';


export class IconbarView extends Component {
  shouldComponentUpdate(nextProps, nextState) {
    //const key = ['iconbar'];
    //return !this.props.state.getIn(key).equals(nextProps.state.getIn(key));

    return true;
  }
  render() {
    const { actions, state } = this.props

    let view;
    if(state.getIn(['iconbar', 'explorer', 'enabled'])) {
      view = (<ExplorerView state={state} actions={actions}/>);
    } else if(state.getIn(['iconbar', 'search', 'enabled'])) {
      view = (<SearchView state={state} actions={actions}/>);
    } else if(state.getIn(['iconbar', 'create', 'enabled'])) {
      view = (<CreateView state={state} actions={actions}/>);
    } else if(state.getIn(['iconbar', 'debug', 'enabled'])) {
      view = (<DebugView state={state} actions={actions}/>);
    } else {
      return null;
    }

    return (<div class="tile is-parent is-2 is-radiusless is-marginless is-paddingless">
      <div class="tile is-child">{view}</div>
    </div>);
  }
}
