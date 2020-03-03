import s from './lib/s';
import Header from './components/Header';

// State
type State = {
  loginName: string;
  password: string;
  isSubmitting: string;
};

// effect


// View
const Login = (state: State): VirtualDOM.VNode => {

  return s('div', {
    style: {},
  },
    s('h2', {}, 'ログイン画面'),
    Header(),
    s('div', {
      style: {
        display: 'flex',
        
      }
    }, [])
  );
};
