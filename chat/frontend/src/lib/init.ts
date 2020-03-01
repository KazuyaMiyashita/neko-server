import createElement from 'virtual-dom/create-element';
import diff from 'virtual-dom/diff';
import { patch } from 'virtual-dom';
import { Root } from '../components/types';

export default <State = {}>(
  root: Root<State>,
  initialState: State,
): {
  dispatch: (partialState: Partial<State>) => void;
  element: Element;
} => {
  const element = createElement(root(initialState));
  const prevNode = root(initialState);
  // 状態だけはmutableに宣言するが隠蔽する
  let state: State = initialState;
  const dispatch = (partialState: Partial<State>): void => {
    state = { ...state, ...partialState };
    const patches = diff(prevNode, root(state));
    patch(element, patches);
  }
  return { element, dispatch };
};
