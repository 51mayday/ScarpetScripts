// Summon Item V1
// By 51mayday
// Provides suggester syntax for summoning item entities with particular motions

__config() -> {
    'commands' -> {
        '<summon_pos> <item_type>' -> ['summon_com', 1, 0, 0, 0],
        '<summon_pos> <item_type> <count>' -> ['summon_com', 0, 0, 0], 
        '<summon_pos> <item_type> <count> <motX> <motY> <motZ>' -> 'summon_com', 
    },
    'arguments' -> {
        'item_type' -> {
            'type' -> 'term',
            'options' -> item_list()
        },
        'summon_pos' -> {
            'type' -> 'location'
        },
        'count' -> {
            'type' -> 'int',
            'min' -> 1,
            'max' -> 64,
            'suggest' -> [1, 16, 64]
        },
        'motX' -> {
            'type' -> 'float',
            'suggest' -> [0]
        },
        'motY' -> {
            'type' -> 'float',
            'suggest' -> [0]
        },
        'motZ' -> {
            'type' -> 'float',
            'suggest' -> [0]
        }

    }
};

summon_com(pos, item_type, count, motionX, motionY, motionZ) -> (
    run(str('summon item %f %f %f {Motion:[%f, %f, %f],Item:{id:"%s",Count:%d}}', pos:0, pos:1, pos:2, motionX, motionY, motionZ, item_type, count));
);