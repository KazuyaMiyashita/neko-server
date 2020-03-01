import h from 'virtual-dom/h';
import { VNode } from 'virtual-dom';

export default (
  tag: string,
  props: VirtualDOM.createProperties,
  ...children: (string | VirtualDOM.VChild)[]
): VNode => h(tag, props, children);
