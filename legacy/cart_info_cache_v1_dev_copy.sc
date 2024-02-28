// Cart Info Cache V1 dev by 51mayday
// Caches position, motion, and inventory of loaded hopper carts for x seconds
// Prints cached values

global_timeout = 10*20;
global_running = false;
global_cart_cache = {};
global_last_tick = tick_time();

__config() -> {
    'commands' -> {
        'set <seconds>' -> '_set_state',
        'start' -> '_start',
        'stop' -> _() -> (global_running = false),
        'print' -> '_print_data',
        'print <gameticks_ago>' -> '_print_data_gt' 
    },
    'arguments' -> {
        'seconds' -> {
            'type' -> 'int',
            'min' -> 0,
            'max' -> 10
        },
        'gameticks_ago' -> {
            'type' -> 'int',
            'min' -> 0,
            'max' -> global_timeout;
        }
    },
    'scope' -> 'global'
};

_set_state(seconds) -> (
    global_timeout = 20*seconds;
    print(player(), str('Set timeout to %d', global_timeout/20));
);

_start() -> (
    global_running = true;
    print(player(), 'Started caching hopper cart positions and motions');
    _runner();
    global_last_tick = tick_time();
);

_runner() -> (
    if(global_running, 
        global_last_tick = tick_time();
        list = [];
        for(entity_list('hopper_minecart'),
            put(list, null, [_~'pos', _~'motion']);
        );

        global_cart_cache:global_last_tick = list;

        for(keys(global_cart_cache),
            if(gt - _ > global_timeout, delete(global_cart_cache, _));
        );

        schedule(1, '_runner');
    );
);

_print_data() -> (
    print(global_cart_cache);
);

_print_data_gt(gametick) -> (
    if(global_last_tick - gametick >= global_timeout, 
        print(player(), str('Cart positions and motions for gametick %d:', global_last_tick - gametick));
        print(player(), format('eb [Summon all carts]', '^w Summon all carts stored for this gametick', '!/script in cart_info_cache_v1 invoke summon_all ' + gametick););
        print(player(), format('rb [Kill all carts]', '^w Kill all carts', '!/script in cart_info_cache_v1 invoke kill_all'););
        for(global_cart_cache:(global_last_tick - gametick),
            command = str('/summon hopper_minecart %f %f %f {Motion:[%f, %f, %f]}', [..._:0, ..._:1]);
            print(format('wb ' + str('[Cart #%d]:', _i), '^w summon cart at pos with mot', '!' + command)); // '!', command
            print(player(), str('Pos: [%f, %f, %f] \nMot: [%f, %f, %f] \n', [_i, ..._:0, ..._:1]));
        );
    );
);

summon_all(gametick) -> (
    for(global_cart_cache:(global_last_tick - gametick),
        run(str('/summon hopper_minecart %f %f %f {Motion:[%f, %f, %f]}', [..._:0, ..._:1]))
    );
);

kill_all() -> (
    for(entity_list('hopper_minecart'),
        modify(_, 'y', -1000);
        modify(_, 'remove');
    );
);
