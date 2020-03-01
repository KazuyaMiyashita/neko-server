export type Root<State = {}> = (state: State) => VirtualDOM.VNode;

export type Component<Props = {}> = (props: Props) => VirtualDOM.VNode;
