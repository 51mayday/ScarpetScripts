// Bartering Simulator by 51mayday
// Spawns bartering drops of num piglins at each position with set x/y/z motion every 120 gameticks
// V2 changes: 
// - Added official support for multiple spawn positions, with different numbers of piglins and different x/y/z motions for each position.
// - Added 'start', 'stop', 'clear', and 'list' functions
// - 'start' starts spawning items for the input settings
// - 'stop' pauses spawning items
// - 'clear' clears stored position settings
// - 'list' lists currently stored positions and settings

__config()  -> {
    'commands' -> {
        '<location> <num> <x_mot> <y_mot> <z_mot>' -> 'add',
        'start' -> 'start',
        'stop' -> 'stop',
        'clear' -> 'clear',
        'list' -> 'list'
    },
    'arguments' -> {
        'num' -> {
            'type' -> 'int',
            'min' -> 1,
            'suggest' -> [64, 576]
        },
        'x_mot' -> {
            'type' -> 'float',
            'suggest' -> [0]
        },
        'y_mot' -> {
            'type' -> 'float',
            'suggest' -> [0]
        },
        'z_mot' -> {
            'type' -> 'float',
            'suggest' -> [0]
        }
    },
    'scope' -> 'global'
};

global_positions = [];
global_running = false;
global_first_run = false;

add(location, num, x_mot, y_mot, z_mot) -> (
    put(global_positions, null, [location, num, [x_mot, y_mot, z_mot]], 'insert');
    print(player(), 'Adding position: [[x_position, y_position, z_position], num_piglins, [x_motion, y_motion, z_motion]]');
    print(player(), [location, num, [x_mot, y_mot, z_mot]]);
);

start() -> (
    print(player(), 'Starting barter item spawning...');
    global_running = true;
    global_first_run = true;
    runner();
);

stop() -> (
    print(player(), 'Pausing barter item spawning...');
    global_running = false;
);

clear() -> (
    print(player(), 'Clearing stored positions...');
    global_positions = [];
);

list() -> (
    print(player(), 'Currently stored positions and settings: [[x_position, y_position, z_position], num_piglins, [x_motion, y_motion, z_motion]]');
    for(global_positions,
        print(player(), _);
    );
);

runner() -> (
    if(global_running && length(global_positions) > 0,
        if(global_first_run, global_first_run = false, 
            for(global_positions,
                position = _:0;
                num = _:1;
                motion = _:2;
                loop(num, 
                    run(str('/loot spawn %f %f %f loot minecraft:gameplay/piglin_bartering', position));
                );

                for(entity_selector(str('@e[x=%f, y=%f, z=%f, distance=0, type=item]', position)),
                    modify(_, 'motion', motion);
                );
            );
        );
        schedule(120, 'runner');
    ,
    if(global_running, print(player(), 'Error: did not find any positions and settings to spawn barter items. Stopping barter item spawning loop.'); global_running = false;);
    );
);