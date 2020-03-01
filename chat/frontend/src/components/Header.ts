import h from 'virtual-dom/h';

export default (...children: VirtualDOM.VNode[]) => h('div', {
  style: {
    display: 'flex',
    width: '100vw',
    position: 'abusolute',
    backgroundColor: '#666',
  }
}, children);
