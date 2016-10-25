package asmcup.runtime;

public class RecordedRobot extends Robot {
	protected final Recorder recorder;
	protected RecordedVM vm;
	
	public RecordedRobot(Recorder recorder, int id, byte[] rom) {
		this(recorder, id, new RecordedVM(rom));
	}
	
	public RecordedRobot(Recorder recorder, int id, RecordedVM vm) {
		super(id, vm);
		this.recorder = recorder;
		this.vm = vm;
	}
	
	@Override
	protected void handleIO(World world) {
		vm.trapIO = true;
		super.handleIO(world);
		vm.trapIO = false;
	}
	
	@Override
	public void tick(World world) {
		super.tick(world);
		
		if (vm.hasRecordedIO()) {
			recorder.record(this, vm.getRecordedIO());
		}
	}
}