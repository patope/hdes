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
const ID = 'editor'
const init = {
  entry: undefined
}



// all explorer actions
const actions = app => update => ({
  init: () => app(({ actions }) => {
  }),
  open: (entry) => app(({ actions }) => {
    update(model => model.setIn([ID, 'entry'], entry));

    const type = entry.get('type');
    if(type === 'fl') {
      actions.editorfl.load();
    } else if(type === 'delete') {
      actions.editordl.load();
    }
  }),

  save: ({entry, value}) => update(model => {


    console.log(entry.toJS(), value)

    return model;
  }),

  close: (entry) => update(model => {
    return model.deleteIn([ID, 'entry']);
  })
})

export const State = app => {
  return {
    id: ID,
    initial: init,
    actions: actions(app)
  }
}
