// V0
// Some util commands I like to use

__config() -> {
    'commands' -> {
        'effects' -> 'giveEffects',
    }
};

giveEffects() -> (
    run('/effect give 51MayDay minecraft:jump_boost infinite 1 true');
    run('/effect give 51MayDay minecraft:speed infinite 1 true')
);