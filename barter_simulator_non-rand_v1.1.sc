// Bartering Simulator by 51mayday
// Spawns bartering drops of N piglins at pos every 120 gameticks
// Items fall straight down
// V1.1 - Changed to +0.3 y motion

__config()  -> {
    'commands' -> {
        '<location> <num>' -> 'start',
        'stop' -> 'stop'
    },
    'arguments' -> {
        'num' -> {
            'type' -> 'int',
            'min' -> 1,
            'suggest' -> [64, 576]
        }
    }
};

global_position = [0, 0, 0];
global_number = 1;
global_running = false;
global_first_run = false;

start(location, num) -> (
    global_position = location;
    global_number = num;
    global_running = true;
    global_first_run = true;
    runner();
);

runner() -> (
    if(global_running,
        if(global_first_run, global_first_run = false, 
            loop(global_number, 
                run(str('/loot spawn %f %f %f loot minecraft:gameplay/piglin_bartering', global_position));
            );

            for(entity_selector(str('@e[x=%f, y=%f, z=%f, distance=0, type=item]', global_position)),
                modify(_, 'motion', [0, 0.3, 0]);
            );
        );
        schedule(120, 'runner');
    );
);

stop() -> (
    global_running = false;
);